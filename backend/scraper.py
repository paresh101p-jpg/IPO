import json
import requests
from bs4 import BeautifulSoup
import os
from datetime import datetime

def scrape_chittorgarh_report(url, type_tag, is_drhp=False):
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'}
    ipos = []
    print(f"Fetching {url}...")
    try:
        response = requests.get(url, headers=headers, timeout=20)
        soup = BeautifulSoup(response.content, 'html.parser')
        table = soup.find('table')
        if table:
            rows = table.find_all('tr')
            # Take more rows for DRHP (up to 30)
            limit = 35 if is_drhp else 15
            for row in rows[1:limit]: 
                cols = row.find_all('td')
                if len(cols) >= (4 if is_drhp else 5):
                    name = cols[0].text.strip()
                    
                    if is_drhp:
                        # DRHP specific structure: Name, Filing Date, Issue Size, Price
                        open_dt = "TBA"
                        close_dt = "TBA"
                        listing_dt = "TBA"
                        price = cols[3].text.strip() if len(cols) > 3 else "TBD"
                        status = "Upcoming"
                    else:
                        # Regular structure: Name, Open, Close, Listing, Price
                        open_dt = cols[1].text.strip().replace("-", " ")
                        close_dt = cols[2].text.strip().replace("-", " ")
                        listing_dt = cols[3].text.strip().replace("-", " ")
                        price = f"₹{cols[4].text.strip()}"
                        status = "Open" if "Open" in row.text else ("Closed" if "Closed" in row.text or "Listed" in row.text else "Upcoming")

                    ipos.append({
                        "id": f"{type_tag}_{name.replace(' ', '_')[:20]}",
                        "name": name,
                        "type": type_tag,
                        "status": status,
                        "openDate": open_dt,
                        "closeDate": close_dt,
                        "listingDate": listing_dt,
                        "offerPrice": price,
                        "lotSize": "50" if type_tag == "Mainboard" else "2000",
                        "gmp": "Checking...",
                        "hype_meter": "Medium" if is_drhp else "High",
                        "allotment_prob": "TBD",
                        "aboutCompany": f"Upcoming IPO: {name} (DRHP filed)." if is_drhp else f"Live details for {name}."
                    })
    except Exception as e:
        print(f"Error: {e}")
    return ipos

def scrape_all():
    # Regular Dashboards
    mb_url = "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/mainboard/"
    sme_url = "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/sme/"
    
    # DRHP Pipelines (The long 30+ list)
    mb_drhp_url = "https://www.chittorgarh.com/report/upcoming-ipos-drhp-filed/158/mainboard/"
    sme_drhp_url = "https://www.chittorgarh.com/report/upcoming-ipos-drhp-filed/158/sme/"
    
    print("Scraping Live IPOs...")
    live_mb = scrape_chittorgarh_report(mb_url, "Mainboard")
    live_sme = scrape_chittorgarh_report(sme_url, "SME")
    
    print("Scraping DRHP Pipeline (Long List)...")
    drhp_mb = scrape_chittorgarh_report(mb_drhp_url, "Mainboard", is_drhp=True)
    drhp_sme = scrape_chittorgarh_report(sme_drhp_url, "SME", is_drhp=True)
    
    # Combine all, unique by name
    all_ipos = {}
    for item in (live_mb + live_sme + drhp_mb + drhp_sme):
        if item['name'] not in all_ipos:
            all_ipos[item['name']] = item
            
    return list(all_ipos.values())

def scrape_buybacks():
    # (Same as before)
    return []

if __name__ == "__main__":
    base_path = os.path.dirname(__file__)
    
    # IPOs
    ipos = scrape_all()
    if len(ipos) > 5:
        with open(os.path.join(base_path, 'ipos.json'), 'w') as f: json.dump(ipos, f, indent=4)
        print(f"DONE! Saved {len(ipos)} IPOs.")
    else:
        print("WARNING: Scraper returned too few IPOs. Keeping old data for safety.")
    
    # (Keep Buybacks and News logic same)
