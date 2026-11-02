#!/usr/bin/env bash
# Régénère les illustrations de démonstration (images/). Demande ImageMagick : nix shell nixpkgs#imagemagick -c ./generate-images.sh
# Ce sont des dessins, pas des photos : ils servent à remplir l'application de démonstration (npm run seed:demo).
set -euo pipefail
cd "$(dirname "$0")/images"

# car <fichier> <couleur carrosserie> <berline|suv|coupe> <ciel haut> <ciel bas>
car() {
  local out=$1 body=$2 kind=$3 top=$4 bottom=$5 lower cabin glass
  case $kind in
    berline) lower="roundrectangle 170,400 1030,598 46,46"; cabin="polygon 330,404 440,282 770,282 890,404"; glass="polygon 362,398 452,298 590,298 590,398 polygon 610,398 610,298 758,298 850,398";;
    suv)     lower="roundrectangle 160,360 1040,602 40,40"; cabin="polygon 250,364 330,250 860,250 950,364"; glass="polygon 284,358 346,266 580,266 580,358 polygon 604,358 604,266 846,266 912,358";;
    coupe)   lower="roundrectangle 160,430 1040,598 56,56"; cabin="polygon 400,434 520,312 740,312 900,434"; glass="polygon 432,428 528,326 640,326 640,428 polygon 662,428 662,326 736,326 850,428";;
  esac
  magick -size 1200x800 gradient:"$top-$bottom" \
    \( -size 1200x800 xc:none -fill "rgba(255,255,255,0.55)" -draw "rectangle 0,590 1200,800" \) -composite \
    \( -size 1200x800 xc:none -fill "rgba(0,0,0,0.35)" -draw "ellipse 600,616 400,24 0,360" -blur 0x12 \) -composite \
    -fill "$body" -stroke "rgba(0,0,0,0.18)" -strokewidth 3 -draw "$cabin" -draw "$lower" \
    -stroke none -fill "rgba(255,255,255,0.22)" -draw "roundrectangle 190,412 1010,444 16,16" \
    -fill "#24313f" -draw "$glass" \
    -fill "rgba(255,255,255,0.16)" -draw "polygon 380,396 470,298 520,298 430,396" \
    -fill "$body" -stroke "rgba(0,0,0,0.25)" -strokewidth 2 -draw "line 600,404 600,560" \
    -stroke none -fill "#fff4c2" -draw "roundrectangle 972,452 1022,492 14,14" \
    -fill "#d8332a" -draw "roundrectangle 178,452 214,490 10,10" \
    -fill "#11161b" -draw "circle 350,598 350,512" -draw "circle 850,598 850,512" \
    -fill "#0b0f13" -draw "circle 350,598 350,520" -draw "circle 850,598 850,520" \
    -fill "#aab3bd" -draw "circle 350,598 350,550" -draw "circle 850,598 850,550" \
    -fill "#e4e9ee" -draw "circle 350,598 350,586" -draw "circle 850,598 850,586" \
    -quality 80 "$out"
}

# avatar <fichier> <initiales> <couleur>
avatar() {
  magick -size 256x256 "xc:$3" -fill white -font DejaVu-Sans-Bold -pointsize 104 -gravity center -annotate +0+4 "$2" -quality 85 "$1"
}

car peugeot.jpg   "#c8a165" berline "#e9f1fa" "#c5d3e3"
car mercedes.jpg  "#1d2433" berline "#eef2f7" "#c9d2de"
car bmw.jpg       "#e3e7ec" coupe   "#dfe9f6" "#b9c9dc"
car audi.jpg      "#b32a2a" suv     "#f3efe6" "#d9d0bd"
car porsche.jpg   "#e2a400" coupe   "#e6edf5" "#c3cedc"
car hyundai.jpg   "#2b6fb8" suv     "#eaf3ee" "#c4d6cb"
car tesla.jpg     "#f0f2f5" berline "#e4ecf6" "#bccbde"
car golf.jpg      "#3d8f5b" berline "#f0f3f8" "#ccd5e1"
car toyota.jpg    "#8a929c" suv     "#eaeef3" "#c8d0da"

avatar avatar-karim.jpg "KB" "#2f7dd1"
avatar avatar-lina.jpg  "LH" "#d1562f"
avatar avatar-yanis.jpg "YM" "#2fa37a"
avatar avatar-sara.jpg  "SA" "#8a4fd1"
avatar avatar-admin.jpg "AG" "#37474f"
