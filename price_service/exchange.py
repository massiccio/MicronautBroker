from flask import Flask
from flask_restful import Api, Resource, reqparse
import datetime
import numpy as np

def generate_data():
    n = 50
    limit_low = 2800
    limit_high = 5000
    my_data = np.random.normal(0, 0.5, n) \
              + np.abs(np.random.normal(0, 2, n) \
                       * np.sin(np.linspace(0, 3*np.pi, n)) ) \
              + np.sin(np.linspace(0, 5*np.pi, n))**2 \
              + np.sin(np.linspace(1, 6*np.pi, n))**2

    scaling = (limit_high - limit_low) / (max(my_data) - min(my_data))
    my_data = my_data * scaling
    my_data = my_data + (limit_low - min(my_data))
    return my_data

class Exchange(Resource):
    
    def __init__(self):
        self.data = generate_data()
        self.index = 0

    def get(self):
        btc = {
            "price": self.data[self.index],
            "timestamp": datetime.datetime.utcnow().isoformat()
        }
        self.index += 1
        return btc, 200

app = Flask(__name__)
api = Api(app)
api.add_resource(Exchange, "/btc-price")
app.run(debug=True)
