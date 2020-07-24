# -*- coding: utf-8 -*-

import json
import requests
import pytemperature
from flask import *

app = Flask(__name__)
#API1
def weather_api():
  
    weather_api_url = "http://api.openweathermap.org/data/2.5/weather?APPID=e070c80006c78825511f6ac2dee259b5&q=Leeds,uk"
    response = requests.get(weather_api_url)
    weather_json_response = response.json()
    kelvin_celcius=pytemperature.k2c(weather_json_response["main"]["temp"])
    return(kelvin_celcius)


  #API2  
def food_api():
    k2c = weather_api()
    
    if k2c >= 7.0 and k2c < 10.0:
        food = "Ratatouille"
        return[food,k2c]
    if k2c>=3 and k2c<5:
        food = "Pasta"
        return[food,k2c]
    if k2c>=5 and k2c<7:
        food = "Lasagne"
        return[food,k2c]
    if k2c>=10.00 :
        food = "Yogurt"
        return[food,k2c]
    if k2c >= 0.0 and k2c <3.0:
        food = "Soup"
        return[food,k2c] 
    else:
        output = "Pray! You're in Antartica."

@app.route("/recipe",methods=['GET'])   
   #API3 
def food_recipe():
    food_name = food_api()
    food_api_url = "http://www.themealdb.com/api/json/v1/1/search.php?s="+food_name[0]
    response = requests.get(food_api_url)
    food_json_response = response.json()
    recipe = food_json_response["meals"][0]["strInstructions"]
    return render_template('ds_cwk2.html',output=recipe,food_name=food_name[0],k2c=int(food_name[1]))

app.run(debug=True)
