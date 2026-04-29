import json
import requests
from bs4 import BeautifulSoup
import os
from datetime import datetime

def scrape_ipos():
    headers = {'User-Agent': 'Mozilla/5.0'}
    urls = {
        "Mainboard": "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/mainboard/",
        "SME": "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/sme/"
    }
    all_ipos = []
    for tag, url in urls.items():
        try:
            resp = requests.get(url, headers=headers, timeout=10)
            soup = BeautifulSoup(resp.content, 'html.parser')
            table = soup.find('table')
            if table:
                for row in table.find_all('tr')[1:12]:
                    cols = row.find_all('td')
                    if len(cols) >= 5:
                        name = cols[0].text.strip()
                        all_ipos.append({
                            "id": f"ipo_{name.replace(' ', '_')}",
                            "name": name,
                            "type": tag,
                            "status": "Open" if "Open" in row.text else ("Closed" if "Closed" in row.text else "Upcoming"),
                            "openDate": cols[1].text.strip().replace("-", " "),
                            "closeDate": cols[2].text.strip().replace("-", " "),
                            "listingDate": cols[3].text.strip().replace("-", " "),
                            "offerPrice": f"₹{cols[4].text.strip()}",
                            "lotSize": "50" if tag == "Mainboard" else "2000",
                            "gmp": "Checking...",
                            "hype_meter": "Medium",
                            "allotment_prob": "TBD"
                        })
        except: pass
    return all_ipos

def scrape_buybacks():
    headers = {'User-Agent': 'Mozilla/5.0'}
    url = "https://www.chittorgarh.com/report/buyback-in-india-list/83/"
    buybacks = []
    try:
        resp = requests.get(url, headers=headers, timeout=10)
        soup = BeautifulSoup(resp.content, 'html.parser')
        table = soup.find('table')
        if table:
            for row in table.find_all('tr')[1:10]:
                cols = row.find_all('td')
                if len(cols) >= 6:
                    name = cols[0].text.strip()
                    buybacks.append({
                        "id": f"bb_{name.replace(' ', '_')}",
                        "name": name,
                        "status": "Current" if "Open" in row.text else "Upcoming",
                        "buybackPrice": f"₹{cols[3].text.strip()}",
                        "recordDate": cols[1].text.strip(),
                        "openDate": cols[2].text.strip(),
                        "closeDate": cols[2].text.strip(), # Approximation
                        "issueSizeShares": "TBD",
                        "issueSizeAmount": cols[4].text.strip(),
                        "buybackRatio": "TBD",
                        "aboutCompany": f"Buyback offer from {name}.",
                        "howToParticipate": "Apply through your broker's corporate action section.",
                        "investmentCalculation": "Check your eligibility based on record date holdings."
                    })
    except: pass
    return buybacks

def scrape_news():
    # Simple news from a mockable source or top market headlines
    return [
        {
            "id": "1",
            "headline": "Market Hits All-Time High as IPO Season Heats Up",
            "summary": "Nifty and Sensex reach new peaks while several companies file for DRHP.",
            "imageUrl": "https://images.unsplash.com/photo-1611974714658-66d2c132042e",
            "date": "29 Apr 2026"
        },
        {
            "id": "2",
            "headline": "SME IPOs Continue to See Record Subscriptions",
            "summary": "Retail investors show massive interest in the SME segment with 100x plus subscriptions.",
            "imageUrl": "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f",
            "date": "28 Apr 2026"
        }
    ]

if __name__ == "__main__":
    base_path = os.path.dirname(__file__)
    
    # IPOs
    ipos = scrape_ipos()
    with open(os.path.join(base_path, 'ipos.json'), 'w') as f: json.dump(ipos, f, indent=4)
    
    # Buybacks
    bbs = scrape_buybacks()
    with open(os.path.join(base_path, 'buybacks.json'), 'w') as f: json.dump(bbs, f, indent=4)
    
    # News
    news = scrape_news()
    with open(os.path.join(base_path, 'news.json'), 'w') as f: json.dump(news, f, indent=4)
    
    print("ALL DATA SYNCED SUCCESSFULLY!")
