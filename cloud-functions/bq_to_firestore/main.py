#!/usr/bin/env python

import json
import base64
import os
import datetime

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
from google.cloud import bigquery
from google.cloud.bigquery import Dataset

# Use the application default credentials
cred = credentials.Certificate('./health-club-demo-firebase-adminsdk-0ny7q-852e8242c6.json')
firebase_admin.initialize_app(cred, {
    'storageBucket': 'health-club-demo.appspot.com'
})
db = firestore.client()
client = bigquery.Client()

# Create a function called "chunks" with two arguments, l and n:
def chunks(l, n):
    # For item i in a range that is a length of l,
    for i in range(0, len(l), n):
        # Create an index range for l of n items:
        yield l[i:i+n]

def generic_bq_to_firestore(query, tableName, key, key2=''):
    query_job = client.query(query, location='US')

    dataset_ref = client.dataset("healthclub", project="health-club-demo")
    table_ref = dataset_ref.table(tableName)
    table = client.get_table(table_ref)  # API Request
    for row in query_job:
        jsonResult = {}
        for schemaField in table.schema:
            if row[schemaField.name] != None:
                if schemaField.field_type == "DATE":
                    jsonResult[schemaField.name] = row[schemaField.name].strftime('%Y-%m-%d')
                elif schemaField.field_type == "TIMESTAMP":
                    jsonResult[schemaField.name] = row[schemaField.name].strftime('%Y-%m-%dT%H:%M:%S.%fZ')
                else:
                    jsonResult[schemaField.name] = row[schemaField.name]

        if key2 != '':            
            doc_id = jsonResult[key2]
        else:
            doc_id = ""
        uid = jsonResult[key]
        print (jsonResult)

        # Add a new doc in collection with ID key
        db.collection('std_member').document(uid).collection(tableName).document(doc_id).set(jsonResult)
    
    print ('Wrote to firestore ' + tableName + '.')
    
def bq_to_firestore():    

    # Copy BigQuery std_member_daily to Firestore
    query = (
            """
            SELECT * FROM `health-club-demo.healthclub.std_member_daily` 
            WHERE aggregated_date > DATE_ADD(CURRENT_DATE(), INTERVAL -30 DAY)
            """
        )        
    generic_bq_to_firestore(query, 'std_member_daily', 'uid', 'aggregated_date')

    # Copy BigQuery std_member_daily_benchmark to Firestore
    query = (
            """
            SELECT * FROM `health-club-demo.healthclub.std_member_daily_benchmark` 
            WHERE aggregated_date > DATE_ADD(CURRENT_DATE(), INTERVAL -30 DAY)
            """
        )        
    generic_bq_to_firestore(query, 'std_member_daily_benchmark', 'uid', 'aggregated_date')

    # Copy BigQuery std_member_incentive_eligibility to Firestore
    query = (
            """
            SELECT * FROM `health-club-demo.healthclub.std_member_incentive_eligibility` 
            WHERE eligible_date > DATE_ADD(CURRENT_DATE(), INTERVAL -30 DAY)
            """
        )        
    generic_bq_to_firestore(query, 'std_member_incentive_eligibility', 'uid', 'eligible_date')

    # Copy BigQuery std_member_recommendation to Firestore
    query = (
            """
            SELECT * FROM `health-club-demo.healthclub.std_member_recommendation` 
            WHERE recommended_date > DATE_ADD(CURRENT_DATE(), INTERVAL -30 DAY)
            """
        )        
    generic_bq_to_firestore(query, 'std_member_recommendation', 'uid', 'recommended_date')



if __name__ == '__main__':
	bq_to_firestore()
    #pass