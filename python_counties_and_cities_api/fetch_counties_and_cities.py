# Fetch data from https://roloca.coldfuse.io/judete and create a json file with the data

import requests
import json

url = "https://roloca.coldfuse.io/judete"

payload = {}
headers = {
    'Cookie': '__cfdui',
    'Accept': 'application/json, text/plain, */*',
    'Accept-Language': 'en-US,en;q=0.5',
    'Referer': 'https://roloca.coldfuse.io/',
    'Connection': 'keep-alive',
    'Pragma': 'no-cache',
    'Cache-Control': 'no-cache',
    'TE': 'Trailers'
}

response = requests.request("GET", url, headers=headers, data=payload)

data = response.json()

with open('counties.json', 'w') as outfile:
    json.dump(data, outfile)

print("Done with counties")

urlCities = "https://roloca.coldfuse.io/orase"

for county in data:
    payload = {}
    headers = {
        'Cookie': '__cfdui',
        'Accept': 'application/json, text/plain, */*',
        'Accept-Language': 'en-US,en;q=0.5',
        'Referer': 'https://roloca.coldfuse.io/',
        'Connection': 'keep-alive',
        'Pragma': 'no-cache',
        'Cache-Control': 'no-cache',
        'TE': 'Trailers'
    }

    response = requests.request("GET", urlCities + '/' + county['auto'], headers=headers, data=payload)

    data = response.json()

    county_data ={
        'county_name' : county['nume'],
        'county_auto' : county['auto'],
        'cities' : data
    }

    with open('cities' + county['auto'] + '.json', 'w') as outfile:
        json.dump(county_data, outfile)

    print("Done with cities")

print("Done with all")