import requests
from bs4 import BeautifulSoup
import json
import time

def fetch_investorgain_live():
    """Fetch live IPO data from investorgain.com"""
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'}
    url = "https://www.investorgain.com/report/live-ipo/331/"
    try:
        resp = requests.get(url, headers=headers, timeout=15)
        print(f"Status: {resp.status_code}")
        if resp.status_code != 200:
            return []
        
        soup = BeautifulSoup(resp.content, 'html.parser')
        
        # Try to find table with IPO data
        tables = soup.find_all('table')
        print(f"Found {len(tables)} tables")
        
        ipos = []
        for table in tables:
            rows = table.find_all('tr')
            print(f"Table has {len(rows)} rows")
            for row in rows[1:]:  # Skip header
                cols = row.find_all('td')
                if len(cols) >= 6:
                    name = cols[0].text.strip()
                    open_date = cols[1].text.strip()
                    close_date = cols[2].text.strip()
                    # Try to get price and lot size
                    price = cols[4].text.strip() if len(cols) > 4 else "TBA"
                    lot_size = cols[5].text.strip() if len(cols) > 5 else "TBA"
                    
                    ipos.append({
                        'name': name,
                        'openDate': open_date,
                        'closeDate': close_date,
                        'offerPrice': price,
                        'lotSize': lot_size
                    })
        
        return ipos
    except Exception as e:
        print(f"Error: {e}")
        return []

def fetch_gmp_data():
    """Fetch GMP data from investorgain"""
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'}
    url = "https://www.investorgain.com/report/live-ipo-gmp/331/"
    gmp_dict = {}
    try:
        resp = requests.get(url, headers=headers, timeout=15)
        if resp.status_code == 200:
            soup = BeautifulSoup(resp.content, 'html.parser')
            tables = soup.find_all('table')
            for table in tables:
                rows = table.find_all('tr')[1:]
                for row in rows:
                    cols = row.find_all('td')
                    if len(cols) >= 2:
                        name = cols[0].text.strip()
                        gmp = cols[1].text.strip()
                        gmp_dict[name] = gmp
    except Exception as e:
        print(f"GMP fetch error: {e}")
    
    return gmp_dict

def update_ipos_json(ipos, gmp_dict):
    """Update ipos.json with real data"""
    # Read existing data
    try:
        with open('backend/ipos.json', 'r') as f:
            existing_data = json.load(f)
    except:
        existing_data = []
    
    # Create new data
    new_data = []
    for ipo in ipos:
        name = ipo['name']
        # Find matching existing entry or create new
        existing = next((item for item in existing_data if item['name'] == name), None)
        
        entry = {
            "id": existing['id'] if existing else f"Mainboard_{name.replace(' ', '_')[:25]}",
            "name": name,
            "type": existing['type'] if existing else "Mainboard",
            "status": existing['status'] if existing else "Upcoming",
            "logoUrl": existing['logoUrl'] if existing else f"https://via.placeholder.com/100/007bff/ffffff?text={name[0]}",
            "openDate": ipo.get('openDate', 'TBA'),
            "closeDate": ipo.get('closeDate', 'TBA'),
            "offerPrice": ipo.get('offerPrice', 'TBA'),
            "lotSize": ipo.get('lotSize', 'TBA'),
            "issueSize": existing.get('issueSize', 'TBA') if existing else 'TBA',
            "gmp": gmp_dict.get(name, 'TBA'),
            "hype_meter": existing.get('hype_meter', 'Medium') if existing else 'Medium',
            "allotment_prob": existing.get('allotment_prob', 'TBD') if existing else 'TBD',
            "aboutCompany": existing.get('aboutCompany', f"Real-time market data for {name}.") if existing else f"Real-time market data for {name}."
        }
        
        # Calculate time remaining if open
        if entry['status'] == 'Open' and entry['closeDate'] != 'TBA':
            # Simple calculation - just show close date
            entry['timeRemaining'] = f"Closes {entry['closeDate']}"
        
        new_data.append(entry)
    
    # Write back
    with open('backend/ipos.json', 'w') as f:
        json.dump(new_data, f, indent=4, ensure_ascii=False)
    
    print(f"Updated {len(new_data)} IPOs in ipos.json")

if __name__ == "__main__":
    print("Fetching live IPO data...")
    ipos = fetch_investorgain_live()
    print(f"Found {len(ipos)} IPOs")
    
    if ipos:
        print("Fetching GMP data...")
        gmp_dict = fetch_gmp_data()
        print(f"Found GMP for {len(gmp_dict)} IPOs")
        
        print("Updating ipos.json...")
        update_ipos_json(ipos, gmp_dict)
    else:
        print("No IPO data found. Check the website structure.")