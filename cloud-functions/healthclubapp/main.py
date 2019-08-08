#!/usr/bin/env python
import sys
import json
from urllib.parse import urlparse
from google.cloud import pubsub_v1
import base64
import os
import datetime
import time
import pytz
import httplib2
import urllib
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
from firebase_admin import auth
from firebase_admin import storage
import random
import uuid

# Use the application default credentials
cred = credentials.Certificate('./health-club-demo-firebase-adminsdk-0ny7q-852e8242c6.json')
firebase_admin.initialize_app(cred, {
    'storageBucket': 'health-club-demo.appspot.com'
})

db = firestore.client()
bucket = storage.bucket()

# Create a function called "chunks" with two arguments, l and n:
def chunks(l, n):
    # For item i in a range that is a length of l,
    for i in range(0, len(l), n):
        # Create an index range for l of n items:
        yield l[i:i+n]

def getHeader(accessToken):
    header = {'Content-Type':'application/json; charset=UTF-8', 
              'Authorization':'Bearer ' + accessToken}    
    return header

def get_from_googlefit_datasource(accessToken):

    url = "https://www.googleapis.com/fitness/v1/users/me/dataSources"
    h = httplib2.Http()
    (resp_headers, content) = h.request(url, "GET", headers=getHeader(accessToken))
    v = json.loads(content)
    #print (v)
    return v

def get_from_googlefit_history(dataType, dataSource, uid, accessToken):
    today = datetime.datetime.utcnow().date()
    unixTime = time.mktime(today.timetuple())
    unixTimeMillis = int(round(unixTime * 1000))
    start_time_string = str(unixTimeMillis - (86400000 * 30))
    end_time_string = str(unixTimeMillis + 86400000)
    #print (start_time_string)
    #print (end_time_string)

    url = "https://www.googleapis.com/fitness/v1/users/me/dataset:aggregate"
    body = ""
    if dataSource != None:
        body = {
            "aggregateBy": [{
                "dataTypeName": dataType,
                "dataSourceId": dataSource
            }],
            "bucketByTime": { "period": { "type" : "day", "value": 1, "timeZoneId" : "UTC"} },
            "startTimeMillis": start_time_string,
            "endTimeMillis": end_time_string
            }
    else:
        body = {
            "aggregateBy": [{
                "dataTypeName": dataType,
                #"dataSourceId": dataSource
            }],
            "bucketByTime": { "period": { "type" : "day", "value": 1, "timeZoneId" : "UTC"} },
            "startTimeMillis": start_time_string,
            "endTimeMillis": end_time_string
            }
        
    h = httplib2.Http()
    (resp_headers, content) = h.request(url, "POST", body=json.dumps(body), headers=getHeader(accessToken))
    v = json.loads(content)
    #print (v)
    return v

def get_from_googlefit_session(uid, accessToken):
    today = datetime.datetime.utcnow().date()
    start_time = today - datetime.timedelta(days=30)
    end_time = today + datetime.timedelta(days=1)
    start_time_string = start_time.strftime('%Y-%m-%dT%H:%M:%S.%fZ')
    end_time_string = end_time.strftime('%Y-%m-%dT%H:%M:%S.%fZ')
    #print (start_time_string)
    #print (end_time_string)

    url = "https://www.googleapis.com/fitness/v1/users/me/sessions?startTime=" + start_time_string + "&endTime=" + end_time_string
       
    h = httplib2.Http()
    (resp_headers, content) = h.request(url, "GET", headers=getHeader(accessToken))
    v = json.loads(content)
    #print (v)
    return v

def save_to_gcs(uid, finalDict):
    
    jsonResult = ""
    # adding uid to final List
    print ("Final Dict - additional processing")
    for key, value in finalDict.items():
        value["uid"] = uid
        value["source"] = "googlefit"
        value["weight"] = "80.2"
        value["load_time"] = datetime.datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ')
        value["record_id"] = str(uuid.uuid1())
        #print (value)
        jsonResult += json.dumps(value) + "\n"

    # Upload the file
    blob = bucket.blob('uploads/users/googlefit/json/' + uid + '.json')
    blob.upload_from_string(jsonResult, content_type='application/json')
    #blob.upload_from_string('', content_type='application/x-www-form-urlencoded;charset=UTF-8')    

    #today = datetime.datetime.utcnow().date()
    #todayString = today.strftime('%Y%m%d')
    print (jsonResult)
    
    print ("Saved to GCS")


def get_save_from_googlefit_daily_data(uid, accessToken):

    finalDict = {}

    print ("\n")
    print ("Steps")
    dataType = "com.google.step_count.delta"
    #dataSource = "derived:com.google.step_count.delta:com.google.android.gms:estimated_steps"
    stepsJson = get_from_googlefit_history(dataType, None, uid, accessToken)
    stepsJsonText = json.dumps(stepsJson, indent=2)
    print (stepsJsonText)

    stepsList = stepsJson["bucket"]
    for item in stepsList:  
        finalItem = {}
        startTimeSeconds = int(item["startTimeMillis"]) / 1000
        startdatetime = datetime.datetime.utcfromtimestamp(startTimeSeconds).strftime('%Y-%m-%d')
        finalItem["aggregated_date"] = startdatetime

        points = item["dataset"][0]["point"]
        if (points):
            subItem = points[0]
            #print (subItem)
            finalItem["steps_count"] = subItem["value"][0]["intVal"]
        else:
            finalItem["steps_count"] = ""
        print (finalItem)
        finalDict[startdatetime] = finalItem

    #print (finalDict)
    print ("\n")

    print ("Calories Expended")
    dataType = "com.google.calories.expended"
    dataSource = "derived:com.google.calories.expended:com.google.android.gms:from_activities"
    #dataSource = "derived:com.google.calories.expended:com.google.android.gms:merge_calories_expended"    
    caloriesExpendedJson = get_from_googlefit_history(dataType, dataSource, uid, accessToken)
    caloriesExpendedJsonText = json.dumps(caloriesExpendedJson, indent=2)
    #print (caloriesExpendedJson)

    caloriesExpendedList = caloriesExpendedJson["bucket"]
    for item in caloriesExpendedList:
        startTimeSeconds = int(item["startTimeMillis"]) / 1000
        startdatetime = datetime.datetime.utcfromtimestamp(startTimeSeconds).strftime('%Y-%m-%d')
        finalItem = finalDict[startdatetime]

        points = item["dataset"][0]["point"]
        if (points):
            subItem = points[0]
            #print (subItem)
            finalItem["calories_burned"] = int(subItem["value"][0]["fpVal"])
        else:
            finalItem["calories_burned"] = ""
        print (finalItem)

    #print (finalDict)
    print ("\n")

    print ("Sleep")
    sleepJson = get_from_googlefit_session(uid, accessToken)
    sleepJsonText = json.dumps(sleepJson, indent=2)
    #print (sleepJsonText)

    sleepList = sleepJson["session"]
    for item in sleepList:
        if item["activityType"] != 72 and item["name"] != "Sleep":
            continue

        endTimeSeconds = int(item["endTimeMillis"]) / 1000
        enddatetime = datetime.datetime.utcfromtimestamp(endTimeSeconds).strftime('%Y-%m-%d')
        finalItem = finalDict[enddatetime]

        startTimeMillis = int(item["startTimeMillis"])
        endTimeMillis = int(item["endTimeMillis"])
        hours = (endTimeMillis - startTimeMillis) / 1000 / 60 / 60
        if "hours_slept" in finalItem:
            finalItem["hours_slept"] += hours
        else:
            finalItem["hours_slept"] = hours
        finalItem["hours_slept"] = round(finalItem["hours_slept"], 2)
        print (finalItem)

    print ("\n")

    print ("Calories Consumed")
    dataType = "com.google.calories.expended"    
    dataSource = "derived:com.google.calories.expended:com.google.android.gms:from_activities"
    caloriesConsumedJson = get_from_googlefit_history(dataType, dataSource, uid, accessToken)
    #caloriesConsumedJsonText = json.dumps(caloriesConsumedJson, indent=2)
    #print (caloriesConsumedJsonText)

    caloriesConsumedList = caloriesConsumedJson["bucket"]
    for item in caloriesConsumedList:
        startTimeSeconds = int(item["startTimeMillis"]) / 1000
        startdatetime = datetime.datetime.utcfromtimestamp(startTimeSeconds).strftime('%Y-%m-%d')
        finalItem = finalDict[startdatetime]

        points = item["dataset"][0]["point"]
        if (points):
            subItem = points[0]
            #print (subItem)
            finalItem["calories_consumed"] = int(subItem["value"][0]["fpVal"])
            finalItem["calories_consumed"] = finalItem["calories_consumed"] + (finalItem["calories_consumed"] * 0.20 * random.randint(-100,100) / 100)
            finalItem["calories_consumed"] = int(finalItem["calories_consumed"])
        else:
            finalItem["calories_consumed"] = ""
        print (finalItem)

    print ("\n")

    # Write to Google Cloud Storage
    save_to_gcs(uid, finalDict)    

    # return Calories Expended for Debugging display
    return caloriesExpendedJson


def process_request(request):

    # Convert data to JSON
    request_json = request.get_json(silent=True)
    print (request_json)

    # Get the Data field, verify the firebase token, and get the Firebase UID
    data = request_json["data"]
    id_token = data["idtoken"]
    access_token = data["accesstoken"]
    print (access_token)
    decoded_token = auth.verify_id_token(id_token)
    print (decoded_token)
    uid = decoded_token["uid"]

    return (uid, access_token)

def read_googlefit_datasources(request):

    # Get data sources from Google Fit Datastore
    (uid, access_token) = process_request(request)    
    result = json.dumps(get_from_googlefit_datasource(access_token), indent=2)
    finalresult = "{ 'data' : " + result + " } "
    print (finalresult)
    return finalresult

def from_googlefit_to_gcs(request):
    (uid, access_token) = process_request(request)    
    result = get_save_from_googlefit_daily_data(uid, access_token)
    result = json.dumps(result)
    finalresult = "{ 'data' : " + result + " } "
    print (finalresult)
    return finalresult


def process_request_debug(request_json):
    data = request_json["data"]
    id_token = data["idtoken"]
    access_token = data["accesstoken"]
    #print (access_token)
    decoded_token = auth.verify_id_token(id_token)
    #print (decoded_token)
    uid = decoded_token["uid"]
    return (uid, access_token)

if __name__ == '__main__':
    request_json = {"data" : {"idtoken":"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"}}
    (uid, access_token) = process_request_debug(request_json)    
    result = get_save_from_googlefit_daily_data(uid, access_token)
    result = json.dumps(result)
    pass
