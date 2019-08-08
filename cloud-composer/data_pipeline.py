# Copyright 2018 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""An example DAG demonstrating simple Apache Airflow operators."""

# [START composer_simple]
from __future__ import print_function

# [START composer_simple_define_dag]
import datetime

from airflow import models
# [END composer_simple_define_dag]

from airflow.operators import bash_operator
from airflow.operators import python_operator
from airflow.contrib.operators.dataflow_operator import \
    DataFlowPythonOperator, DataFlowJavaOperator, \
    DataflowTemplateOperator, GoogleCloudBucketHelper
from airflow.contrib.operators.bigquery_operator import BigQueryOperator
from airflow.contrib.operators.bigquery_to_bigquery import BigQueryToBigQueryOperator
from airflow.contrib.operators.gcs_to_bq import GoogleCloudStorageToBigQueryOperator
from google.cloud import bigquery

# [START composer_simple_define_dag]
default_dag_args = {
    # The start_date describes when a DAG is valid / can be run. Set this to a
    # fixed point in time rather than dynamically, since it is evaluated every
    # time a DAG is parsed. See:
    # https://airflow.apache.org/faq.html#what-s-the-deal-with-start-date
    'start_date': datetime.datetime(2019, 3, 16),
}

# Define a DAG (directed acyclic graph) of tasks.
# Any task you create within the context manager is automatically added to the
# DAG object.
with models.DAG(
        'healthclub_data_pipeline',
        schedule_interval=datetime.timedelta(days=1),
        default_args=default_dag_args) as dag:

    # Step 1 - Pull from GCS to BigQuery
    TASK_ID = 'step1_gcs_to_bigquery'
    TEMPLATE = 'gs://dataflow-templates/2019-03-15-00/GCS_Text_to_BigQuery'
    PARAMETERS = {
        'javascriptTextTransformGcsPath': 'gs://health-club-etl/scripts/batch_googlefit_json.js',
        'JSONPath': 'gs://health-club-etl/scripts/batch_googlefit_schema.json',
        'javascriptTextTransformFunctionName': 'transform',
        'outputTable': 'health-club-demo:healthclub.batch_googlefit_json',
        'inputFilePattern': 'gs://health-club-demo.appspot.com/uploads/users/googlefit/json/*',
        'bigQueryLoadingTemporaryDirectory': 'gs://health-club-etl/temp_dir',
    }
    DEFAULT_OPTIONS_TEMPLATE = {
        'project': 'health-club-demo',
        'stagingLocation': 'gs://health-club-etl/staging',
        'tempLocation': 'gs://health-club-etl/tmp',
        'zone': 'us-central1-f'
    }
    POLL_SLEEP = 30

    step1 = DataflowTemplateOperator(
            task_id=TASK_ID,
            template=TEMPLATE,
            parameters=PARAMETERS,
            dataflow_default_options=DEFAULT_OPTIONS_TEMPLATE,
            poll_sleep=POLL_SLEEP)


    # Step 2 - BigQuery batch table ( csv, json, xml ) to standardized BigQuery table    
    TASK_ID = 'step2_json_xml_csv_to_merged'
    step2 = BigQueryToBigQueryOperator(
            task_id=TASK_ID,
            source_project_dataset_tables=['health-club-demo:healthclub.batch_googlefit_json', 'health-club-demo:healthclub.batch_applehealth_xml', 'health-club-demo:healthclub.batch_fitbit_csv'],
            destination_project_dataset_table='health-club-demo:healthclub.merged_member_daily',
            write_disposition='WRITE_APPEND',
            create_disposition='CREATE_NEVER',
            dataflow_default_options=DEFAULT_OPTIONS_TEMPLATE,
            poll_sleep=POLL_SLEEP)


    # Step 3 - Pull firestore "std_member" to BigQuery, Mock implementation due to Time
    TASK_ID = 'step3_std_member_firestore_to_bigquery'
    step3 = bash_operator.BashOperator(
            task_id=TASK_ID,
            bash_command='echo step3_std_member_firestore_to_bigquery completed.')

    # Step 4 - Retent only 1 years of data counting from membership start date
    TASK_ID = 'step4_retent_1year_memberdata'
    step4 = BigQueryOperator(
            task_id=TASK_ID,
            sql = """
                DELETE healthclub.merged_member_daily AS d
                WHERE d.record_id IN
                (
                    SELECT d.record_id
                    FROM healthclub.merged_member_daily d
                    INNER JOIN healthclub.std_member m ON m.uid = d.uid
                    WHERE d.aggregated_date < m.membership_start_date
                    AND DATE_DIFF(d.aggregated_date, m.membership_start_date, day) > 365
                );""",
            use_legacy_sql = False,
            dataflow_default_options=DEFAULT_OPTIONS_TEMPLATE,
            poll_sleep=POLL_SLEEP)


    # Step 5 - De-duplicate by SELECT DISTINCT
    TASK_ID = 'step5_merged_to_std_dedup_record_id'    
    step5 = BigQueryOperator(
            task_id=TASK_ID,
            sql = """
                SELECT DISTINCT record_id, aggregated_date, source, uid, weight, 
                hours_slept, calories_consumed, calories_burned,
                steps_count, load_time
                FROM healthclub.merged_member_daily;
                """,
            use_legacy_sql = False,
            destination_dataset_table='health-club-demo:healthclub.std_member_daily',
            write_disposition ='WRITE_TRUNCATE',
            create_disposition='CREATE_NEVER',
            dataflow_default_options=DEFAULT_OPTIONS_TEMPLATE,
            poll_sleep=POLL_SLEEP)

    # Step 6 - De-duplicate by Load Time
    TASK_ID = 'step6_dedup_load_time'
    step6 = BigQueryOperator(
            task_id=TASK_ID,
            sql = """
            DELETE healthclub.std_member_daily AS d
                WHERE d.record_id NOT IN
                (
                    SELECT record_id
                    FROM
                    (
                        SELECT record_id,
                        row_number() OVER (PARTITION BY aggregated_date, source, uid ORDER BY load_time DESC) row_number
                        FROM healthclub.std_member_daily
                    )
                   WHERE row_number = 1
                );""",
            use_legacy_sql = False,
            dataflow_default_options=DEFAULT_OPTIONS_TEMPLATE,
            poll_sleep=POLL_SLEEP)

    # Step 7a - Refresh benchmark files - Calorie Needs
    TASK_ID = 'step7a_refresh_bench_caleroie_needs'
    #step7a = GoogleCloudStorageToBigQueryOperator(
    #        task_id=TASK_ID,
    #        bucket="health-club-etl",
    #        source_objects="cdc_calorie_needs_lookup.csv",
    #        destination_project_dataset_table='health-club-demo:healthclub.bench_calorie_needs',
    #        schema_object="bench_calorie_needs_schema.json",
    #        source_format="CSV",
    #        skip_leading_rows=1,
    #        write_disposition="WRITE_TRUNCATE",
    #        dataflow_default_options=DEFAULT_OPTIONS_TEMPLATE,
    #        poll_sleep=POLL_SLEEP)
    step7a = bash_operator.BashOperator(
             task_id=TASK_ID,
             bash_command='echo ' + TASK_ID + '.')    

    # Step 7b - Refresh benchmark files - Sleep Hours
    TASK_ID = 'step7b_refresh_bench_sleep_hours'
    #step7b = GoogleCloudStorageToBigQueryOperator(
    #        task_id=TASK_ID,
    #        bucket="health-club-etl",
    #        source_objects="cdc_sleep_hours_lookup.csv",
    #        destination_project_dataset_table='health-club-demo:healthclub.bench_sleep_hours',
    #        schema_object="bench_sleep_hours_schema.json",
    #        source_format="CSV",
    #        skip_leading_rows=1,
    #        write_disposition="WRITE_TRUNCATE",
    #        dataflow_default_options=DEFAULT_OPTIONS_TEMPLATE,
    #        poll_sleep=POLL_SLEEP)
    step7b = bash_operator.BashOperator(
             task_id=TASK_ID,
             bash_command='echo ' + TASK_ID + '.')

    # Step 8 - Do benchmark comparison
    TASK_ID = 'step8_benchmark_comparison'
    step8 = BigQueryOperator(
            task_id=TASK_ID,
            sql = """
                SELECT d.uid, d.aggregated_date, d.source, m.age, d.weight, d.hours_slept, d.calories_consumed, d.calories_burned,d.steps_count, 
                bc.Sedentary, bc.Moderately_Active, bc.Active, 
                CASE
                    WHEN calories_burned >= bc.Active THEN 'Active'
                    WHEN calories_burned >= bc.Moderately_Active AND calories_burned < bc.Active THEN 'Moderately_Active'
                    WHEN calories_burned < bc.Moderately_Active THEN 'Sedentary'
                END cdc_calories_burned_status,
                bs.Age_Group, bs.Min_Sleep_Hours_Per_Day, bs.Max_Sleep_Hours_Per_Day,
                CASE
                    WHEN hours_slept > bs.Max_Sleep_Hours_Per_Day THEN 'Over-sleeping'
                    WHEN hours_slept >= bs.Min_Sleep_Hours_Per_Day AND hours_slept <= Max_Sleep_Hours_Per_Day THEN 'Enough sleep'
                    WHEN hours_slept < bs.Min_Sleep_Hours_Per_Day THEN 'Under-sleeping'
                END cdc_sleep_status
                FROM healthclub.std_member_daily d
                INNER JOIN healthclub.std_member m
                ON d.uid = m.uid
                LEFT JOIN healthclub.bench_calorie_needs bc
                ON bc.Gender = m.gender 
                AND m.age >= bc.Min_age AND m.age <= bc.Max_Age
                INNER JOIN healthclub.bench_sleep_hours bs
                ON m.age >= bs.Min_age AND m.age <= bs.Max_Age;
                """,
            use_legacy_sql = False,
            destination_dataset_table='health-club-demo:healthclub.std_member_daily_benchmark',
            write_disposition ='WRITE_TRUNCATE',
            create_disposition='CREATE_NEVER',
            dataflow_default_options=DEFAULT_OPTIONS_TEMPLATE,
            poll_sleep=POLL_SLEEP)

    # Step 9 - Recommendation - Mock

    # E.g. 1. Detect that user has been under-sleep for 5 days in a row, suggest to take more sleep the next day
    # E.g. 2. Detect that user has been sedentary for 5 days in a row, suggest to do more exercise
    #         Such as taking the training session at the health club
    # E.g. 3. If both condition, under-sleeping and sedentary for more than 7 days, send warning alert
    #         trigger SMS notification

    TASK_ID = 'step9_recommendation'
    step9 = bash_operator.BashOperator(
             task_id=TASK_ID,
             bash_command='echo ' + TASK_ID + '.')

    # Step 10 - Check Incentive Program eligibility - Mock
    
    # E.g. 1. Awaken Program, 10 days being sedmentary and over-sleeping in a row
    #         Eligibie to join the program, and have 15% discount on membership fee
    #         Award discount condition, the next 10 days need to be "Active" and "Enough Sleep"

    # E.g. 2. Being Human, 20 consective days being "under-sleeping" with less than 5 hours sleep
    #         Eligibie to join the program, and have 10% discount on membership fee
    #         Award discount condition, the next 10 days need to be "Moderately Active" and "Enough Sleep"

    # E.g. 3. Eat too much without exercise, 15 days calories consumed are 30% more than calories burned
    #         Eligibie to join the program, and have 10% discount on membership fee
    #         Award discount condition, the next 10 days need to be balanced eater,
    #         calories consumed and burned difference within 15% gap.

    TASK_ID = 'step10_incentive_program_eligibility'
    step10 = bash_operator.BashOperator(
             task_id=TASK_ID,
             bash_command='echo ' + TASK_ID + '.')

    # Step 11 - Save daily / benchmark comparison / Recommendation to firestore - Mock
    # Have a Cloud Function doing this    
    TASK_ID = 'step11_bigquery_to_firestore'
    step11 = bash_operator.BashOperator(
             task_id=TASK_ID,
             bash_command='echo ' + TASK_ID + '.')

    # Step 12 - Send Email / Mobile notification to members - Mock
    TASK_ID = 'step12_send_notification'
    step12 = bash_operator.BashOperator(
             task_id=TASK_ID,
             bash_command='echo ' + TASK_ID + '.')

    # Step 13 - Trigger dashboard / reporting refresh if any
    TASK_ID = 'step13_trigger_dashboard_reporting_refresh'
    step13 = bash_operator.BashOperator(
             task_id=TASK_ID,
             bash_command='echo ' + TASK_ID + '.')    

    # Step 1 - Pull from GCS to BigQuery
    # Step 2 - BigQuery batch table ( csv, json, xml ) to standardized BigQuery table
    # Step 3 - Pull firestore "std_member" to BigQuery - Mock
    # Step 4 - Retent only 1 years of data counting from membership start date
    # Step 5 - De-duplicate by SELECT DISTINCT
    # Step 6 - De-duplicate by Load Time
    # Step 7 - Refresh benchmark files
    # Step 8 - Do benchmark comparison
    # Step 9 - Recommendation - Mock
    # Step 10 - Check Incentive Program eligibility - Mock
    # Step 11 - Save daily / benchmark comparison / Recommendation to firestore - Mock
    # Step 12 - Send Email / Mobile notification - Mock
    # Step 13 - Trigger dashboard / reporting refresh - Mock

    step1 >> step2 >> step3 >> step4 >> step5 >> step6 >> step8 >> step9 >> step10 >> step11 >> step12
    step7a >> step7b >> step8
    step11 >> step13



