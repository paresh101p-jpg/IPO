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

def scrape_section(url, type_tag):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }
    ipos = []
    try:
        response = requests.get(url, headers=headers)
        soup = BeautifulSoup(response.content, 'html.parser')
        table = soup.find('table', {'class': 'table'}) or soup.find('table')
        
        if table:
            rows = table.find('tbody').find_all('tr')
            for i, row in enumerate(rows[:15]):
                cols = row.find_all('td')
                if len(cols) >= 8:
                    name = cols[0].text.strip()
                    gmp = cols[2].text.strip()
                    open_date = cols[5].text.strip()
                    close_date = cols[6].text.strip()
                    status = cols[7].text.strip()
                    subscription = cols[4].text.strip() if cols[4].text.strip() else "0x"
                    
                    ipos.append({
                        "id": f"{type_tag}_{i}",
                        "name": name, # Removed tag from name
                        "type": type_tag, # Keep type for filtering
                        "gmp": gmp,
                        "status": status,
                        "openDate": open_date,
                        "closeDate": close_date,
                        "subscription": subscription,
                        "allotment_prob": calculate_allotment(subscription),
                        "hype_meter": determine_hype(subscription),
                        "aboutCompany": f"{name} is a company in the {type_tag} segment."
                    })
    except Exception as e:
        print(f"Error scraping {type_tag}: {e}")
    return ipos

def scrape_all():
    mainboard_url = "https://www.investorgain.com/report/live-ipo-gmp/331/ipo/"
    sme_url = "https://www.investorgain.com/report/live-ipo-gmp/331/sme/"
    
    print("Scraping Mainboard IPOs...")
    mainboard_ipos = scrape_section(mainboard_url, "Mainboard")
    
    print("Scraping SME IPOs...")
    sme_ipos = scrape_section(sme_url, "SME")
    
    return mainboard_ipos + sme_ipos

if __name__ == "__main__":
    all_data = scrape_all()
    if all_data:
        file_path = os.path.join(os.path.dirname(__file__), 'ipos.json')
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(all_data, f, ensure_ascii=False, indent=4)
        print(f"Successfully saved {len(all_data)} IPOs to {file_path}")
    else:
        print("No data scraped. Check website structure.")
