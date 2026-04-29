import json
from flask import Flask, jsonify

app = Flask(__name__)

def scrape_ipo_data():
    """
    Scrape real IPO data.
    This mock data simulates real-time data fetched from online sources.
    """
    scraped_ipos = [
        {
            "id": "1",
            "name": "Awfis Space Solutions",
            "gmp": "₹115 (30% Gain)",
            "status": "Upcoming",
            "openDate": "22 May",
            "closeDate": "27 May",
            "priceBand": "₹364 to ₹383",
            "subscription": "0x",
            "allotment_prob": "TBD",
            "hype_meter": "High",
            "red_flags": ["Low margin business"]
        },
        {
            "id": "2",
            "name": "Go Digit General Insurance",
            "gmp": "₹10 (3.6% Gain)",
            "status": "Closed",
            "openDate": "15 May",
            "closeDate": "17 May",
            "priceBand": "₹258 to ₹272",
            "subscription": "9.60x",
            "allotment_prob": "10%",
            "hype_meter": "Medium",
            "red_flags": ["Regulatory warnings in past"]
        },
        {
            "id": "3",
            "name": "Aadhar Housing Finance",
            "gmp": "₹60 (19% Gain)",
            "status": "Listed",
            "openDate": "08 May",
            "closeDate": "10 May",
            "priceBand": "₹300 to ₹315",
            "subscription": "26.76x",
            "allotment_prob": "4%",
            "hype_meter": "Very High",
            "red_flags": ["None"]
        }
    ]
    return scraped_ipos

@app.route('/api/ipos', methods=['GET'])
def get_ipos():
    data = scrape_ipo_data()
    return jsonify(data)

@app.route('/', methods=['GET'])
def home():
    return "IPO Tracker API is running online!"

if __name__ == '__main__':
    # This is for local testing. PythonAnywhere will use the 'app' variable directly.
    app.run(debug=True, host='0.0.0.0', port=8000)
