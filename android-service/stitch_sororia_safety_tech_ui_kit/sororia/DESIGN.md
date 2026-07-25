---
name: SororIA
colors:
  surface: '#f5fafc'
  surface-dim: '#d6dbdd'
  surface-bright: '#f5fafc'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4f6'
  surface-container: '#eaeff1'
  surface-container-high: '#e4e9eb'
  surface-container-highest: '#dee3e5'
  on-surface: '#171c1e'
  on-surface-variant: '#4b4452'
  inverse-surface: '#2c3133'
  inverse-on-surface: '#ecf1f3'
  outline: '#7d7483'
  outline-variant: '#cec3d4'
  surface-tint: '#7a3fbb'
  primary: '#3f0075'
  on-primary: '#ffffff'
  primary-container: '#5a189a'
  on-primary-container: '#c896ff'
  inverse-primary: '#dbb8ff'
  secondary: '#006878'
  on-secondary: '#ffffff'
  secondary-container: '#69e5ff'
  on-secondary-container: '#006575'
  tertiary: '#00311f'
  on-tertiary: '#ffffff'
  tertiary-container: '#004a30'
  on-tertiary-container: '#54bf8e'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#efdbff'
  primary-fixed-dim: '#dbb8ff'
  on-primary-fixed: '#2b0052'
  on-primary-fixed-variant: '#6122a1'
  secondary-fixed: '#a7edff'
  secondary-fixed-dim: '#58d6f1'
  on-secondary-fixed: '#001f25'
  on-secondary-fixed-variant: '#004e5b'
  tertiary-fixed: '#8df7c2'
  tertiary-fixed-dim: '#71daa7'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005235'
  background: '#f5fafc'
  on-background: '#171c1e'
  surface-variant: '#dee3e5'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  critical-data:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  touch-target-min: 44px
  button-height: 48px
  input-height: 52px
  list-item-height: 64px
---

## Brand & Style
The design system is centered on trust, safety, and rapid utility for a femtech safety application. The brand personality is protective yet empowering, utilizing a **Corporate Modern** aesthetic with a focus on high-reliability patterns. The UI must feel calm and professional to reduce user anxiety during high-stress situations, while maintaining a high level of accessibility. 

Key attributes include:
- **Immediate Clarity:** Information is prioritized based on urgency.
- **Empowerment:** Use of strong violets to symbolize solidarity and strength.
- **Discretion:** A dual-purpose visual language that includes a "Camouflage" mode for user privacy and safety.

## Colors
The palette is divided into functional roles to ensure users can differentiate between standard actions and emergency triggers.

- **Primary (Deep Violet):** Reserved for brand moments and primary "Save/Proceed" actions.
- **Secondary (Serene Blue):** Used for supportive details, icons, and non-critical information.
- **Semantic Colors:** Emerald Green for success, Amber for warnings, and Crimson Red exclusively for critical emergency triggers or destructive actions.
- **Background & Surface:** A Light Gray foundation with Pure White cards ensures a clean, layered hierarchy.
- **Camouflage Mode:** When activated, the system strips all brand colors in favor of a strictly neutral Black/White/Charcoal palette to mimic a utility or system application.

## Typography
The system uses **Inter** for its exceptional legibility and neutral tone. 

- **Scale:** High contrast is maintained between body text (14px) and critical data (16-18px Bold) to ensure readability under duress or in low-light conditions.
- **Labels:** Smallest text is capped at 12px to remain accessible.
- **Hierarchy:** Headlines use a tighter tracking and heavier weight to anchor sections.

## Layout & Spacing
This design system is optimized for a **Mobile-First** fixed grid based on a 390x844px viewport. 

- **The 8pt Grid:** All spacing and component dimensions are derived from a 4px/8px base unit.
- **Touch Targets:** A strict minimum of 44px for all interactive elements to ensure accuracy during movement.
- **Safe Areas:** Layouts must account for a 16px horizontal margin and respect the bottom navigation bar height to avoid overlap.

## Elevation & Depth
Depth is used functionally to indicate interactable surfaces.
- **Level 0 (Background):** #EDF2F4 (Light Gray).
- **Level 1 (Cards/Surfaces):** Pure White with a very soft, diffused shadow (0px 4px 12px, 5% opacity black).
- **Level 2 (Modals/Sheets):** High-elevation surfaces that appear from the bottom, using a 10% opacity shadow to separate them from the content beneath.
- **Camouflage Mode Depth:** Elevation is flattened to use 1px borders instead of shadows, reducing visual distinctiveness.

## Shapes
The shape language is friendly yet structured.
- **Base Components:** 16px (1rem) corner radius for cards and containers.
- **Buttons & Inputs:** 8px (0.5rem) radius for a more technical and reliable feel.
- **Bottom Sheets:** Only the top-left and top-right corners are rounded (16px) to maintain a cohesive "sliding up" metaphor.

## Components
- **Primary Buttons:** 48px height, full-width by default. Uses the Deep Violet background with White text.
- **Critical Buttons:** Reserved for SOS or emergency triggers. Crimson Red background.
- **Inputs:** 52px height featuring floating labels that move to 12px text on focus, ensuring the user never loses context of the field name.
- **Lists:** 64px height cells to provide ample vertical touch space. Includes a trailing chevron icon for navigational clarity.
- **Bottom Navigation:** A fixed bar containing 4 items (Inicio, Historial, Red de Apoyo, Ajustes). Active states use the Deep Violet color for the icon and label.
- **Bottom Sheets:** Replacing all traditional modals, these slide from the bottom and occupy between 50% and 90% of the screen height depending on content density.
- **Chips:** Small 32px height indicators for status (e.g., "Active", "Encrypted") using secondary or success colors with 10% opacity backgrounds.