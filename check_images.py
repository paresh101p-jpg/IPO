import os
from PIL import Image

dir_path = r"C:\Users\Admin\.gemini\antigravity\brain\ed67a871-f973-453c-9095-25590f5c9e50"
img1_path = os.path.join(dir_path, "media__1777449892171.png")
img2_path = os.path.join(dir_path, "media__1777449923719.png")

img1 = Image.open(img1_path)
img2 = Image.open(img2_path)

print(f"Image 1: {img1_path} - Size: {img1.size}")
print(f"Image 2: {img2_path} - Size: {img2.size}")
