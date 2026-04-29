import json
import requests
from bs4 import BeautifulSoup
import os
from datetime import datetime

def scrape_gmp():
    headers = {'User-Agent': 'Mozilla/5.0'}
    url = "https://www.chittorgarh.com/report/ipo-grey-market-premium-gmp-chittorgarh/2/"
    gmp_data = {}
    try:
        resp = requests.get(url, headers=headers, timeout=15)
        soup = BeautifulSoup(resp.content, 'html.parser')
        table = soup.find('table')
        if table:
            for row in table.find_all('tr')[1:]:
                cols = row.find_all('td')
                if len(cols) >= 3:
                    name = cols[0].text.strip().split(' IPO')[0]
                    gmp = cols[1].text.strip()
                    gmp_data[name] = gmp
    except: pass
    return gmp_data

def scrape_chittorgarh_report(url, type_tag, is_drhp=False):
    headers = {'User-Agent': 'Mozilla/5.0'}
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
                if len(cols) >= 4:
                    name = cols[0].text.strip()
                    open_dt = cols[1].text.strip().replace("-", " ")
                    close_dt = cols[2].text.strip().replace("-", " ")
                    price = cols[3].text.strip() if is_drhp else f"₹{cols[4].text.strip()}"
                    status = "Open" if "Open" in row.text else ("Closed" if ("Closed" in row.text or "Listed" in row.text) else "Upcoming")

                    ipos.append({
                        "name": name,
                        "type": type_tag,
                        "status": status,
                        "openDate": open_dt,
                        "closeDate": close_dt,
                        "offerPrice": price,
                        "lotSize": "50" if type_tag == "Mainboard" else "2000"
                    })
    except: pass
    return ipos

if __name__ == "__main__":
    base_path = os.path.dirname(__file__)
    
    # 1. Fetch GMP first
    gmps = scrape_gmp()
    
    # 2. Fetch IPOs
    mb_live = scrape_chittorgarh_report("https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/mainboard/", "Mainboard")
    sme_live = scrape_chittorgarh_report("https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/sme/", "SME")
    mb_drhp = scrape_chittorgarh_report("https://www.chittorgarh.com/report/upcoming-ipos-drhp-filed/158/mainboard/", "Mainboard", True)
    sme_drhp = scrape_chittorgarh_report("https://www.chittorgarh.com/report/upcoming-ipos-drhp-filed/158/sme/", "SME", True)
    
    all_ipos = {}
    for item in (mb_live + sme_live + mb_drhp + sme_drhp):
        name = item['name']
        if name not in all_ipos:
            # Add GMP if found
            found_gmp = "TBA" if item['status'] == "Upcoming" else "N/A"
            for k in gmps:
                if k.lower() in name.lower():
                    found_gmp = gmps[k]
                    break
            
            item["id"] = f"{item['type']}_{name.replace(' ', '_')[:25]}"
            item["gmp"] = found_gmp
            item["hype_meter"] = "High" if "₹" in found_gmp else "Medium"
            item["allotment_prob"] = "TBD"
            item["aboutCompany"] = f"Market data for {name}."
            all_ipos[name] = item
            
    final_list = list(all_ipos.values())
    if len(final_list) > 10:
        with open(os.path.join(base_path, 'ipos.json'), 'w') as f:
            json.dump(final_list, f, indent=4)
        print(f"Synced {len(final_list)} IPOs with Real GMP.")
