import json
import os
from flask import Flask, jsonify
from scraper import scrape_and_save_ipos

app = Flask(__name__)
BASE_PATH = os.path.dirname(__file__)
IPOS_FILE = os.path.join(BASE_PATH, 'ipos.json')


def load_ipos_data():
    if os.path.exists(IPOS_FILE):
        with open(IPOS_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    return scrape_and_save_ipos()


@app.route('/api/ipos', methods=['GET'])
def get_ipos():
    data = load_ipos_data()
    return jsonify(data)


@app.route('/api/sync', methods=['POST'])
def sync_ipos():
    data = scrape_and_save_ipos()
    return jsonify({"status": "ok", "count": len(data), "ipos": data})


@app.route('/', methods=['GET'])
def home():
    return "IPO Tracker API is running online!"


if __name__ == '__main__':
    # This is for local testing. PythonAnywhere will use the 'app' variable directly.
    app.run(debug=True, host='0.0.0.0', port=8000)
