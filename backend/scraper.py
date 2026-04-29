import json
import requests
from bs4 import BeautifulSoup
import os
from datetime import datetime

def scrape_chittorgarh_report(url, type_tag, is_drhp=False):
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'}
    ipos = []
    try:
        response = requests.get(url, headers=headers, timeout=20)
        soup = BeautifulSoup(response.content, 'html.parser')
        table = soup.find('table')
        if table:
            rows = table.find_all('tr')
            limit = 40 if is_drhp else 25
            for row in rows[1:limit]: 
                cols = row.find_all('td')
                if len(cols) >= (4 if is_drhp else 5):
                    name = cols[0].text.strip()
                    if is_drhp:
                        open_dt, close_dt, listing_dt = "TBA", "TBA", "TBA"
                        price = cols[3].text.strip() if len(cols) > 3 else "TBD"
                        status = "Upcoming"
                    else:
                        open_dt = cols[1].text.strip().replace("-", " ")
                        close_dt = cols[2].text.strip().replace("-", " ")
                        listing_dt = cols[3].text.strip().replace("-", " ")
                        price = f"₹{cols[4].text.strip()}"
                        status = "Open" if "Open" in row.text else ("Closed" if ("Closed" in row.text or "Listed" in row.text or "Result" in row.text) else "Upcoming")

                    ipos.append({
                        "id": f"{type_tag}_{name.replace(' ', '_')[:25]}",
                        "name": name,
                        "type": type_tag,
                        "status": status,
                        "openDate": open_dt,
                        "closeDate": close_dt,
                        "listingDate": listing_dt,
                        "offerPrice": price,
                        "lotSize": "50" if type_tag == "Mainboard" else "2000",
                        "gmp": "Live...",
                        "hype_meter": "Medium",
                        "allotment_prob": "TBD",
                        "aboutCompany": f"Latest update for {name}."
                    })
    except: pass
    return ipos

def scrape_buybacks():
    headers = {'User-Agent': 'Mozilla/5.0'}
    url = "https://www.chittorgarh.com/report/buyback-in-india-list/83/"
    buybacks = []
    try:
        resp = requests.get(url, headers=headers, timeout=20)
        soup = BeautifulSoup(resp.content, 'html.parser')
        table = soup.find('table')
        if table:
            for row in table.find_all('tr')[1:20]:
                cols = row.find_all('td')
                if len(cols) >= 6:
                    name = cols[0].text.strip()
                    buybacks.append({
                        "id": f"bb_{name.replace(' ', '_')[:25]}",
                        "name": name,
                        "status": "Current" if "Open" in row.text else "Upcoming",
                        "buybackPrice": f"₹{cols[3].text.strip()}",
                        "recordDate": cols[1].text.strip(),
                        "openDate": cols[2].text.strip(),
                        "closeDate": cols[2].text.strip(),
                        "issueSizeAmount": cols[4].text.strip(),
                        "aboutCompany": f"Buyback offer from {name}.",
                        "howToParticipate": "Apply via broker portal."
                    })
    except: pass
    return buybacks

if __name__ == "__main__":
    base_path = os.path.dirname(__file__)
    
    # IPOs
    print("Syncing IPOs (Live + DRHP)...")
    live_mb = scrape_chittorgarh_report("https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/mainboard/", "Mainboard")
    live_sme = scrape_chittorgarh_report("https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/sme/", "SME")
    drhp_mb = scrape_chittorgarh_report("https://www.chittorgarh.com/report/upcoming-ipos-drhp-filed/158/mainboard/", "Mainboard", True)
    drhp_sme = scrape_chittorgarh_report("https://www.chittorgarh.com/report/upcoming-ipos-drhp-filed/158/sme/", "SME", True)
    
    all_ipos = {}
    for item in (live_mb + live_sme + drhp_mb + drhp_sme):
        if item['name'] not in all_ipos: all_ipos[item['name']] = item
    
    ipos_list = list(all_ipos.values())
    if len(ipos_list) > 10:
        with open(os.path.join(base_path, 'ipos.json'), 'w') as f: json.dump(ipos_list, f, indent=4)
        print(f"IPOs Synced: {len(ipos_list)}")

    # Buybacks
    print("Syncing Buybacks...")
    bbs = scrape_buybacks()
    if len(bbs) > 2:
        with open(os.path.join(base_path, 'buybacks.json'), 'w') as f: json.dump(bbs, f, indent=4)
        print(f"Buybacks Synced: {len(bbs)}")
    
    print("ALL DONE.")
