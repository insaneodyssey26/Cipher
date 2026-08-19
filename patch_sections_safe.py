with open("app/src/main/java/com/masum/cipher/ui/settings/SettingsScreen.kt", "r") as f: content = f.read()

# Replace TRACKING & INTEGRATIONS and SMART RULES
content = content.replace('SettingsSection("TRACKING & INTEGRATIONS", icon = LucideIcons.Activity, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "TRACKING & INTEGRATIONS", onToggle = { expandedSection = if (expandedSection == "TRACKING & INTEGRATIONS") null else "TRACKING & INTEGRATIONS" }) {',
                          'SettingsSection("AUTOMATION & TRACKING", icon = LucideIcons.Activity, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "AUTOMATION & TRACKING", onToggle = { expandedSection = if (expandedSection == "AUTOMATION & TRACKING") null else "AUTOMATION & TRACKING" }) {')

content = content.replace('SettingsSection("SMART RULES", icon = LucideIcons.Wand, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "SMART RULES", onToggle = { expandedSection = if (expandedSection == "SMART RULES") null else "SMART RULES" }) {', '')

# Replace DATA MANAGEMENT and AUTO-BACKUP
content = content.replace('SettingsSection("DATA MANAGEMENT", icon = LucideIcons.Database, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "DATA MANAGEMENT", onToggle = { expandedSection = if (expandedSection == "DATA MANAGEMENT") null else "DATA MANAGEMENT" }) {',
                          'SettingsSection("DATA & BACKUP", icon = LucideIcons.Database, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "DATA & BACKUP", onToggle = { expandedSection = if (expandedSection == "DATA & BACKUP") null else "DATA & BACKUP" }) {')

content = content.replace('SettingsSection("AUTO-BACKUP", icon = LucideIcons.Cloud, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "AUTO-BACKUP", onToggle = { expandedSection = if (expandedSection == "AUTO-BACKUP") null else "AUTO-BACKUP" }) {', '')

# Replace SUPPORT & FEEDBACK and ABOUT CIPHER
content = content.replace('SettingsSection("SUPPORT & FEEDBACK", icon = LucideIcons.MessageSquare, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "SUPPORT & FEEDBACK", onToggle = { expandedSection = if (expandedSection == "SUPPORT & FEEDBACK") null else "SUPPORT & FEEDBACK" }) {',
                          'SettingsSection("ABOUT & SUPPORT", icon = LucideIcons.Info, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "ABOUT & SUPPORT", onToggle = { expandedSection = if (expandedSection == "ABOUT & SUPPORT") null else "ABOUT & SUPPORT" }) {')

content = content.replace('SettingsSection("ABOUT CIPHER", icon = LucideIcons.Info, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "ABOUT CIPHER", onToggle = { expandedSection = if (expandedSection == "ABOUT CIPHER") null else "ABOUT CIPHER" }) {', '')

import re
# Now carefully remove the unmatched closing braces for the sections we removed
# Each removed header left a dangling `}` before it.
# Wait, actually, let's just delete the `}\n\n` before the removed headers!
# But since I already removed the headers with replace(..., ''), there is just `}\n\n` followed by the contents!
# We just need to remove one `}` for each combined section!
