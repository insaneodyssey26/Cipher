from PIL import Image, ImageDraw, ImageFont
import os

# Create a 240x240 transparent image
img = Image.new('RGBA', (240, 240), (255, 255, 255, 0))
draw = ImageDraw.Draw(img)

# Try to use a default font, bold
try:
    font = ImageFont.truetype("DejaVuSans-Bold.ttf", 64)
except:
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 64)
    except:
        font = ImageFont.load_default()

# Draw text in the center
text = "cipher"
# get bounding box
bbox = draw.textbbox((0, 0), text, font=font)
text_w = bbox[2] - bbox[0]
text_h = bbox[3] - bbox[1]

x = (240 - text_w) / 2
y = (240 - text_h) / 2 - bbox[1]

draw.text((x, y), text, fill=(255, 255, 255, 255), font=font)

# Save to Android res
os.makedirs("app/src/main/res/drawable", exist_ok=True)
img.save("app/src/main/res/drawable/ic_cipher_text.png")
print("Icon generated!")
