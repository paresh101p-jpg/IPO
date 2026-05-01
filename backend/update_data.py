import requests
from bs4 import BeautifulSoup
import json
import time

def get_gmp_for_ipo(name):
    """Return GMP for a given IPO name."""
    try:
        headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"}
        r = requests.get("https://ipowatch.in/ipo-grey-market-premium-latest-ipo-gmp/", headers=headers, timeout=10)
        if r.status_code == 200:
            soup = BeautifulSoup(r.content, "html.parser")
            for table in soup.find_all("table"):
                for row in table.find_all("tr")[1:]:
                    cols = row.find_all(["td", "th"])
                    if len(cols) >= 2:
                        ipo_name = cols[0].text.strip()
                        if name.lower() in ipo_name.lower() or ipo_name.lower() in name.lower():
                            return cols[1].text.strip()
    except Exception:
        pass

    # Fallback static values
    static = {
        "OnEMI Technology": "Rs4",
        "Recode Studios": "Rs11",
        "Bagmane REIT": "Rs0",
        "Amba Auto Sales": "Rs0",
        "Swiggy": "TBA"
    }
    for key, val in static.items():
        if key.lower() in name.lower():
            return val
    return "TBA"

def create_realistic_data():
    ipos = [
        {
            "id": "Mainboard_OnEMI_Technology_Solutions",
            "name": "OnEMI Technology Solutions Ltd (Kissht)",
            "type": "Mainboard",
            "status": "Open",
            "logoUrl": "https://via.placeholder.com/100/007bff/ffffff?text=O",
            "openDate": "30 Apr 2026",
            "closeDate": "05 May 2026",
            "timeRemaining": "5d 6h",
            "offerPrice": "Rs162 - Rs171",
            "lotSize": "87",
            "issueSize": "Rs925.92 Cr",
            "gmp": "Rs4",
            "hype_meter": "Medium",
            "allotment_prob": "TBD",
            "aboutCompany": "OnEMI (Kissht) is a leading fintech platform providing instant credit and personal loans."
        },
        {
            "id": "SME_Recode_Studios",
            "name": "Recode Studios",
            "type": "SME",
            "status": "Upcoming",
            "logoUrl": "https://via.placeholder.com/100/9c27b0/ffffff?text=R",
            "openDate": "05 May 2026",
            "closeDate": "07 May 2026",
            "offerPrice": "Rs150 - Rs158",
            "lotSize": "800",
            "issueSize": "Rs22.40 Cr",
            "gmp": "Rs11",
            "hype_meter": "High",
            "allotment_prob": "TBD",
            "aboutCompany": "Recode Studios is a cosmetic and wellness brand known for its high-quality products."
        },
        {
            "id": "Mainboard_Bagmane_Prime_Office_REIT",
            "name": "Bagmane Prime Office REIT",
            "type": "Mainboard",
            "status": "Upcoming",
            "logoUrl": "https://via.placeholder.com/100/4caf50/ffffff?text=B",
            "openDate": "05 May 2026",
            "closeDate": "07 May 2026",
            "offerPrice": "Rs100",
            "lotSize": "1",
            "issueSize": "Rs2200 Cr",
            "gmp": "Rs0",
            "hype_meter": "Medium",
            "allotment_prob": "TBD",
            "aboutCompany": "Bagmane Prime Office REIT is a real estate investment trust focusing on high-quality office spaces in Bangalore."
        },
        {
            "id": "SME_Amba_Auto_Sales",
            "name": "Amba Auto Sales",
            "type": "SME",
            "status": "Open",
            "logoUrl": "https://via.placeholder.com/100/ff5722/ffffff?text=A",
            "openDate": "27 Apr 2026",
            "closeDate": "29 Apr 2026",
            "offerPrice": "Rs66 - Rs70",
            "lotSize": "2000",