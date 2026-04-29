import json
import requests
from bs4 import BeautifulSoup
import os

def determine_hype(subscription_text):
    try:
        sub = float(subscription_text.replace('x', '').strip())
        if sub > 50: return "Very High"
        if sub > 15: return "High"
        if sub > 5: return "Medium"
        return "Low"
    except:
        return "Medium"

def calculate_allotment(subscription_text):
    try:
        sub = float(subscription_text.replace('x', '').strip())
        if sub <= 1: return "100%"
        prob = (1 / sub) * 100
        return f"{prob:.1f}%"
    except:
        return "TBD"

def scrape_ipo_data():
    url = "https://www.investorgain.com/report/live-ipo-gmp/331/"
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }
    
    try:
        response = requests.get(url, headers=headers)
        soup = BeautifulSoup(response.content, 'html.parser')
        table = soup.find('table', {'id': 'DataTables_Table_0'})
        
        ipos = []
        if not table:
            # Fallback mock data if table changes
            return [{"id":"1","name":"Mock Data (Scraper Failed)","gmp":"₹0","status":"Error","subscription":"0x","allotment_prob":"0%","hype_meter":"Low","red_flags":["Scraping error"]}]

        rows = table.find('tbody').find_all('tr')
        for i, row in enumerate(rows[:10]): # Get top 10 recent IPOs
            cols = row.find_all('td')
            if len(cols) >= 8:
                name = cols[0].text.strip()
                gmp = cols[2].text.strip()
                open_date = cols[5].text.strip()
                close_date = cols[6].text.strip()
                status = cols[7].text.strip()
                subscription = cols[4].text.strip() if cols[4].text.strip() else "0x"
                
                # Some dummy red flags logic based on GMP
                red_flags = ["None"]
                if "0" in gmp and len(gmp) < 5:
                    red_flags = ["Low demand in grey market", "High risk"]

                ipos.append({
                    "id": str(i+1),
                    "name": name,
                    "gmp": gmp,
                    "status": status,
                    "openDate": open_date,
                    "closeDate": close_date,
                    "priceBand": "TBD",
                    "subscription": subscription,
                    "allotment_prob": calculate_allotment(subscription),
                    "hype_meter": determine_hype(subscription),
                    "red_flags": red_flags
                })
        return ipos
    except Exception as e:
        print(f"Error scraping: {e}")
        return []

if __name__ == "__main__":
    print("Scraping live IPO data...")
    data = scrape_ipo_data()
    
    # Save to JSON file
    file_path = os.path.join(os.path.dirname(__file__), 'ipos.json')
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=4)
    print(f"Successfully saved {len(data)} IPOs to {file_path}")
