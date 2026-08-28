#!/usr/bin/env python3
import os
import sys
import math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

REPO_ROOT = Path(__file__).resolve().parent.parent
RAW_DIR = REPO_ROOT / "screenshots" / "raw"
OUTPUT_DIR = REPO_ROOT / "screenshots" / "play_store"
FONT_DIR = REPO_ROOT / "app" / "src" / "main" / "res" / "font"

CANVAS_WIDTH = 1080
CANVAS_HEIGHT = 2400

BG_COLOR = (8, 11, 16)

SLIDES_CONFIG = [
    {
        "filename": "01_dashboard.png",
        "output_name": "01_dashboard_mockup.png",
        "badge": "LOCAL-FIRST & 100% PRIVATE",
        "headline": "Private. Powerful. Fast.",
        "subtitle": "Automated expense tracking with zero cloud servers.",
        "accent_color": (99, 102, 241),
        "accent_hex": "#6366F1",
    },
    {
        "filename": "02_financial_flow.png",
        "output_name": "02_financial_flow_mockup.png",
        "badge": "FINANCIAL FLOW TRENDS",
        "headline": "Understand Your Cash Flow.",
        "subtitle": "Interactive expense, income, and net curves with scrubbing.",
        "accent_color": (6, 182, 212),
        "accent_hex": "#06B6D4",
    },
    {
        "filename": "03_subscriptions.png",
        "output_name": "03_subscriptions_mockup.png",
        "badge": "RECURRING BILLS HUB",
        "headline": "Master Every Subscription.",
        "subtitle": "Monthly commitments, countdowns, and renewal alerts.",
        "accent_color": (139, 92, 246),
        "accent_hex": "#8B5CF6",
    },
    {
        "filename": "04_smart_rules.png",
        "output_name": "04_smart_rules_mockup.png",
        "badge": "AUTOMATIC CATEGORIZATION",
        "headline": "Zero Manual Tagging.",
        "subtitle": "Smart rules learn and categorize future transactions automatically.",
        "accent_color": (245, 158, 11),
        "accent_hex": "#F59E0B",
    },
    {
        "filename": "05_security_themes.png",
        "output_name": "05_security_themes_mockup.png",
        "badge": "ENCRYPTED & PERSONALIZED",
        "headline": "Bank-Grade Security.",
        "subtitle": "AES-GCM encrypted backups and 10 dynamic accent themes.",
        "accent_color": (16, 185, 129),
        "accent_hex": "#10B981",
    }
]

def load_font(font_name: str, size: int):
    font_path = FONT_DIR / font_name
    if font_path.exists():
        try:
            return ImageFont.truetype(str(font_path), size)
        except Exception:
            pass
    try:
        return ImageFont.truetype("DejaVuSans-Bold.ttf" if "bold" in font_name.lower() or "black" in font_name.lower() else "DejaVuSans.ttf", size)
    except Exception:
        return ImageFont.load_default()

def draw_radial_glow(canvas: Image.Image, center_x: int, center_y: int, radius: int, color: tuple, max_alpha: int = 40):
    glow_layer = Image.new("RGBA", (CANVAS_WIDTH, CANVAS_HEIGHT), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow_layer)
    
    steps = 45
    for i in range(steps, 0, -1):
        current_r = int(radius * (i / steps))
        factor = 1.0 - (i / steps)
        alpha = int(max_alpha * (factor ** 2.2))
        fill_color = (*color, alpha)
        glow_draw.ellipse(
            [center_x - current_r, center_y - current_r, center_x + current_r, center_y + current_r],
            fill=fill_color
        )
    
    glow_layer = glow_layer.filter(ImageFilter.GaussianBlur(radius=55))
    canvas.alpha_composite(glow_layer)

def round_corners(image: Image.Image, radius: int) -> Image.Image:
    mask = Image.new("L", image.size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle([(0, 0), image.size], radius=radius, fill=255)
    result = Image.new("RGBA", image.size, (0, 0, 0, 0))
    result.paste(image, (0, 0), mask=mask)
    return result

def create_device_mockup(screenshot_path: Path, target_w: int = 760, target_h: int = 1600, corner_radius: int = 54) -> Image.Image:
    if screenshot_path and screenshot_path.exists():
        raw_screen = Image.open(screenshot_path).convert("RGBA")
    else:
        raw_screen = Image.new("RGBA", (target_w, target_h), (18, 22, 32, 255))
        draw_ph = ImageDraw.Draw(raw_screen)
        draw_ph.text((target_w // 2, target_h // 2), "Drop Screenshot Here", fill=(120, 130, 150), anchor="mm")

    screen_aspect = raw_screen.width / raw_screen.height
    target_aspect = target_w / target_h

    if screen_aspect > target_aspect:
        new_w = int(raw_screen.height * target_aspect)
        offset_x = (raw_screen.width - new_w) // 2
        raw_screen = raw_screen.crop((offset_x, 0, offset_x + new_w, raw_screen.height))
    else:
        new_h = int(raw_screen.width / target_aspect)
        offset_y = (raw_screen.height - new_h) // 2
        raw_screen = raw_screen.crop((0, offset_y, raw_screen.width, offset_y + new_h))

    screen_resized = raw_screen.resize((target_w, target_h), Image.Resampling.LANCZOS)
    screen_rounded = round_corners(screen_resized, corner_radius)

    bezel_thickness = 14
    outer_w = target_w + (bezel_thickness * 2)
    outer_h = target_h + (bezel_thickness * 2)
    outer_radius = corner_radius + bezel_thickness

    device_frame = Image.new("RGBA", (outer_w, outer_h), (0, 0, 0, 0))
    draw_frame = ImageDraw.Draw(device_frame)

    draw_frame.rounded_rectangle(
        [(0, 0), (outer_w, outer_h)],
        radius=outer_radius,
        fill=(22, 26, 35, 255),
        outline=(50, 56, 72, 255),
        width=2
    )

    device_frame.paste(screen_rounded, (bezel_thickness, bezel_thickness), mask=screen_rounded)

    inner_highlight = Image.new("RGBA", (outer_w, outer_h), (0, 0, 0, 0))
    draw_hl = ImageDraw.Draw(inner_highlight)
    draw_hl.rounded_rectangle(
        [(bezel_thickness - 1, bezel_thickness - 1), (outer_w - bezel_thickness + 1, outer_h - bezel_thickness + 1)],
        radius=corner_radius + 1,
        outline=(255, 255, 255, 20),
        width=1
    )
    device_frame.alpha_composite(inner_highlight)

    shadow_margin = 120
    canvas_w = outer_w + (shadow_margin * 2)
    canvas_h = outer_h + (shadow_margin * 2)
    shadowed_device = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))

    ambient_shadow = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
    draw_amb = ImageDraw.Draw(ambient_shadow)
    draw_amb.rounded_rectangle(
        [
            (shadow_margin + 6, shadow_margin + 24),
            (shadow_margin + outer_w - 6, shadow_margin + outer_h + 24)
        ],
        radius=outer_radius + 4,
        fill=(0, 0, 0, 160)
    )
    ambient_shadow = ambient_shadow.filter(ImageFilter.GaussianBlur(radius=48))

    contact_shadow = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
    draw_cnt = ImageDraw.Draw(contact_shadow)
    draw_cnt.rounded_rectangle(
        [
            (shadow_margin, shadow_margin + 12),
            (shadow_margin + outer_w, shadow_margin + outer_h + 12)
        ],
        radius=outer_radius,
        fill=(0, 0, 0, 220)
    )
    contact_shadow = contact_shadow.filter(ImageFilter.GaussianBlur(radius=18))

    shadowed_device.alpha_composite(ambient_shadow)
    shadowed_device.alpha_composite(contact_shadow)
    shadowed_device.paste(device_frame, (shadow_margin, shadow_margin), mask=device_frame)

    return shadowed_device

def render_slide(config: dict) -> Path:
    canvas = Image.new("RGBA", (CANVAS_WIDTH, CANVAS_HEIGHT), (*BG_COLOR, 255))
    accent_rgb = config["accent_color"]

    draw_radial_glow(
        canvas=canvas,
        center_x=CANVAS_WIDTH // 2,
        center_y=1400,
        radius=650,
        color=accent_rgb,
        max_alpha=55
    )

    draw = ImageDraw.Draw(canvas)

    badge_text = config["badge"]
    font_badge = load_font("dmsans_variable.ttf", 28)
    badge_bbox = font_badge.getbbox(badge_text)
    badge_text_w = badge_bbox[2] - badge_bbox[0]
    badge_text_h = badge_bbox[3] - badge_bbox[1]

    badge_pad_x = 24
    badge_pad_y = 10
    badge_w = badge_text_w + (badge_pad_x * 2)
    badge_h = badge_text_h + (badge_pad_y * 2)
    badge_x = (CANVAS_WIDTH - badge_w) // 2
    badge_y = 140

    badge_layer = Image.new("RGBA", (CANVAS_WIDTH, CANVAS_HEIGHT), (0, 0, 0, 0))
    badge_draw = ImageDraw.Draw(badge_layer)
    badge_draw.rounded_rectangle(
        [(badge_x, badge_y), (badge_x + badge_w, badge_y + badge_h)],
        radius=badge_h // 2,
        fill=(*accent_rgb, 28),
        outline=(*accent_rgb, 90),
        width=2
    )
    badge_draw.text(
        (badge_x + badge_pad_x, badge_y + badge_pad_y - 2),
        badge_text,
        font=font_badge,
        fill=(*accent_rgb, 255)
    )
    canvas.alpha_composite(badge_layer)

    headline_text = config["headline"]
    font_headline = load_font("dmsans_variable.ttf", 74)
    draw.text(
        (CANVAS_WIDTH // 2, 280),
        headline_text,
        font=font_headline,
        fill=(255, 255, 255, 255),
        anchor="mt"
    )

    subtitle_text = config["subtitle"]
    font_subtitle = load_font("dmsans_variable.ttf", 36)
    
    words = subtitle_text.split()
    lines = []
    curr_line = []
    for word in words:
        curr_line.append(word)
        test_line = " ".join(curr_line)
        bbox = font_subtitle.getbbox(test_line)
        if (bbox[2] - bbox[0]) > 860:
            curr_line.pop()
            lines.append(" ".join(curr_line))
            curr_line = [word]
    if curr_line:
        lines.append(" ".join(curr_line))

    sub_y = 390
    for line in lines:
        draw.text(
            (CANVAS_WIDTH // 2, sub_y),
            line,
            font=font_subtitle,
            fill=(148, 163, 184, 255),
            anchor="mt"
        )
        sub_y += 50

    screenshot_file = RAW_DIR / config["filename"]
    device_img = create_device_mockup(
        screenshot_path=screenshot_file,
        target_w=780,
        target_h=1620,
        corner_radius=56
    )

    dev_x = (CANVAS_WIDTH - device_img.width) // 2
    dev_y = 560
    canvas.alpha_composite(device_img, (dev_x, dev_y))

    output_path = OUTPUT_DIR / config["output_name"]
    final_output = canvas.convert("RGB")
    final_output.save(output_path, "PNG", quality=100)
    return output_path

def main():
    RAW_DIR.mkdir(parents=True, exist_ok=True)
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    print("==================================================")
    print("  Cipher - High-End Play Store Mockup Generator   ")
    print("==================================================")
    print(f"Raw Screenshot Source : {RAW_DIR}")
    print(f"Play Store Output Dir : {OUTPUT_DIR}\n")

    generated = []
    for idx, slide in enumerate(SLIDES_CONFIG, 1):
        print(f"[{idx}/{len(SLIDES_CONFIG)}] Generating '{slide['output_name']}' with {slide['accent_hex']} accent...")
        out_file = render_slide(slide)
        generated.append(out_file)

    print("\nAll 5 premium mockups successfully created in:")
    print(f"  --> {OUTPUT_DIR}")

if __name__ == "__main__":
    main()
