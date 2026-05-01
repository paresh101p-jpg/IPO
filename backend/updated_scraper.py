import json
import re
import requests
from bs4 import BeautifulSoup
import os
import time

# Use robust headers to avoid being blocked
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8',
    'Accept-Language': 'en-US,en;q=0.9',
    'Referer': 'https://www.google.com/'
}

def get_session():
    session = requests.Session()
    session.headers.update(HEADERS)
    return session

def fetch_gmp():
    """Fetch GMP from multiple sources for reliability."""
    gmp_data = {}
    
    # Source 1: IPOWatch
    print("Fetching GMP from IPOWatch...")
    try:
        r = requests.get("https://ipowatch.in/ipo-grey-market-premium-latest-ipo-gmp/", headers=HEADERS, timeout=10)
        soup = BeautifulSoup(r.content, 'html.parser')
        table = soup.find('table')
        if table:
            for row in table.find_all('tr')[1:]:
                cols = row.find_all('td')
                if len(cols) >= 2:
                    name = cols[0].text.strip().split(' IPO')[0].strip()
                    gmp = cols[1].text.strip()
                    if gmp in ['-', '--', '0']: gmp = "Rs0"
                    elif not gmp.startswith('Rs'): gmp = f"Rs{gmp}"
                    gmp_data[name] = gmp
    except Exception as e:
        print(f"IPOWatch Error: {e}")

    # Source 2: InvestorGain (as fallback/supplement)
    print("Fetching GMP from InvestorGain...")
    try:
        r = requests.get("https://www.investorgain.com/report/live-ipo-gmp/331/", headers=HEADERS, timeout=10)
        soup = BeautifulSoup(r.content, 'html.parser')
        for row in soup.find_all('tr')[1:]:
            cols = row.find_all('td')
            if len(cols) >= 3:
                name = cols[0].text.strip()
                gmp = cols[2].text.strip()
                if name not in gmp_data:
                    if gmp in ['-', '--', '0']: gmp = "Rs0"
                    elif not gmp.startswith('Rs'): gmp = f"Rs{gmp}"
                    gmp_data[name] = gmp
    except Exception as e:
        print(f"InvestorGain GMP Error: {e}")

    return gmp_data

def fetch_ipo_list():
    """Fetch the main IPO list from InvestorGain (more reliable table)."""
    print("Fetching IPO list from InvestorGain...")
    ipos = []
    try:
        r = requests.get("https://www.investorgain.com/report/live-ipo-gmp/331/", headers=HEADERS, timeout=15)
        soup = BeautifulSoup(r.content, 'html.parser')
        # Find the main table
        table = soup.find('table')
        if table:
            for row in table.find_all('tr')[1:]:
                cols = row.find_all('td')
                if len(cols) >= 6:
                    name = cols[0].text.strip()
                    gmp = cols[2].text.strip()
                    price = cols[3].text.strip()
                    gain = cols[5].text.strip()
                    
                    # Determine status and dates from text or sub-pages (simplified for now)
                    status = "Upcoming"
                    if "Open" in row.text: status = "Open"
                    elif "Closed" in row.text: status = "Closed"
                    
                    # Link to detail page
                    a = cols[0].find('a')
                    detail_url = a['href'] if a else None
                    
                    ipos.append({
                        "name": name,
                        "gmp": f"Rs{gmp}" if gmp and gmp != '-' else "TBA",
                        "offerPrice": f"Rs{price}" if price and price != '-' else "TBA",
                        "status": status,
                        "detailUrl": detail_url
                    })
    except Exception as e:
        print(f"List Fetch Error: {e}")
    return ipos

def get_detail_info(url):
    """Fetch lot size and dates from detail page."""
    if not url: return {}
    try:
        r = requests.get(url, headers=HEADERS, timeout=10)
        soup = BeautifulSoup(r.content, 'html.parser')
        text = soup.get_text()
        
        data = {}
        # Simple regex for lot size
        lot_match = re.search(r'Lot Size[:\s]*(\d+)', text, re.I)
        data['lotSize'] = lot_match.group(1) if lot_match else "TBA"
        
        # Simple regex for dates
        date_match = re.search(r'Open[:\s]*([\w\d\s,]+)', text, re.I)
        data['openDate'] = date_match.group(1).strip() if date_match else "TBA"
        
        return data
    except:
        return {}

def main():
    gmps = fetch_gmp()
    ipo_list = fetch_ipo_list()
    
    final_data = []
    for item in ipo_list:
        name = item['name']
        print(f"Processing {name}...")
        
        # Enrich with GMP from either source
        item['gmp'] = gmps.get(name, item['gmp'])
        
        # Determine hype
        gmp_val = item['gmp'].replace('Rs', '').replace(',', '').strip()
        try:
            item['hype_meter'] = "High" if float(gmp_val) > 20 else "Medium"
        except:
            item['hype_meter'] = "Medium"
            
        # Add placeholder logos/ids
        item['id'] = f"IPO_{name.replace(' ', '_')[:20]}"
        item['logoUrl'] = f"https://via.placeholder.com/100/007bff/ffffff?text={name[0]}"
        
        final_data.append(item)
    
    # Save to file
    base_path = os.path.dirname(__file__)
    with open(os.path.join(base_path, 'ipos.json'), 'w') as f:
        json.dump(final_data, f, indent=4)
    print(f"Updated {len(final_data)} IPOs in ipos.json")

if __name__ == "__main__":
    main()
