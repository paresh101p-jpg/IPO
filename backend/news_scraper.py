import requests
import json
import os
import re

def fetch_ipo_news():
    print("Fetching IPO news from Google News...")
    url = "https://news.google.com/rss/search?q=IPO+India&hl=en-IN&gl=IN&ceid=IN:en"
    news_items = []
    try:
        resp = requests.get(url, timeout=10)
        content = resp.text
        
        # Extremely simple regex parsing for RSS to avoid dependencies
        items = re.findall(r'<item>(.*?)</item>', content, re.S)
        
        for i, item in enumerate(items[:8]):
            title_match = re.search(r'<title>(.*?)</title>', item)
            link_match = re.search(r'<link>(.*?)</link>', item)
            pub_match = re.search(r'<pubDate>(.*?)</pubDate>', item)
            
            if title_match and link_match:
                title = title_match.group(1)
                headline = title.split(' - ')[0]
                source = title.split(' - ')[-1] if ' - ' in title else "Market News"
                
                news_items.append({
                    "id": str(i + 1),
                    "headline": headline,
                    "summary": f"Latest IPO news from {source}. Click to view details.",
                    "imageUrl": f"https://images.unsplash.com/photo-1611974714658-66d2c132042e?auto=format&fit=crop&q=80&w=300",
                    "date": pub_match.group(1)[:16] if pub_match else "Recently",
                    "url": link_match.group(1)
                })
    except Exception as e:
        print(f"News Fetch Error: {e}")
    
    return news_items

if __name__ == "__main__":
    news = fetch_ipo_news()
    if news:
        base_path = os.path.dirname(__file__)
        with open(os.path.join(base_path, 'news.json'), 'w') as f:
            json.dump(news, f, indent=4)
        print(f"Successfully updated {len(news)} news items.")
