package com.example.miniprojet;

import android.content.Context;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.color.MaterialColors;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.NumberFormat;
import java.util.Locale;

/** Petits utilitaires d'affichage partagés par les écrans : prix, pastilles de type et d'état. */
public final class Ui {
    private Ui() {
    }

    /** « 4 850 000 DA », et « 12 000 DA / jour » pour une location. */
    public static String price(Context ctx, String price, String type) {
        String shown = price;
        try {
            BigDecimal value = new BigDecimal(price);
            NumberFormat format = NumberFormat.getIntegerInstance(Locale.FRANCE);
            shown = format.format(value.setScale(0, java.math.RoundingMode.HALF_UP)).replace(' ', ' ');
        } catch (NumberFormatException | NullPointerException ignored) {
            // on affiche tel quel ce que le serveur a rendu
        }
        String text = shown + " " + ctx.getString(R.string.currency);
        if ("Location".equals(type)) {
            text += " " + ctx.getString(R.string.per_day);
        }
        return text;
    }

    public static String km(Context ctx, String kilometrage) {
        try {
            return NumberFormat.getIntegerInstance(Locale.FRANCE).format(Long.parseLong(kilometrage)).replace(' ', ' ') + " " + ctx.getString(R.string.km);
        } catch (NumberFormatException | NullPointerException e) {
            return kilometrage + " " + ctx.getString(R.string.km);
        }
    }

    /** L'heure d'un message (« 09:41 »), précédée du jour s'il n'est pas d'aujourd'hui (« 30 oct. 18:02 »). */
    public static String messageFooter(JSONObject message) {
        String raw = message.optString("date", "");
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(raw.replace('T', ' ').substring(0, 19));
            boolean today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date).equals(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
            return new SimpleDateFormat(today ? "HH:mm" : "d MMM HH:mm", Locale.FRANCE).format(date);
        } catch (ParseException | RuntimeException e) {
            return raw;
        }
    }

    /** La pastille « Vente » / « Location » (les valeurs envoyées au serveur restent « Vente » et « Location »). */
    public static void typePill(TextView view, String type) {
        boolean rental = "Location".equals(type);
        view.setText(rental ? R.string.rental : R.string.sale);
        view.setBackgroundResource(rental ? R.drawable.bg_pill_location : R.drawable.bg_pill_neutral);
        view.setTextColor(MaterialColors.getColor(view, rental ? com.google.android.material.R.attr.colorOnSecondaryContainer : com.google.android.material.R.attr.colorOnPrimaryContainer));
    }

    /** La pastille d'état d'une demande : En attente / Acceptée / Refusée. */
    public static void statePill(TextView view, String state) {
        int label;
        int background;
        int color;
        if ("Accepted".equals(state)) {
            label = R.string.state_accepted;
            background = R.drawable.bg_pill_accepted;
            color = ContextCompat.getColor(view.getContext(), R.color.gc_on_success_container);
        } else if ("Rejected".equals(state)) {
            label = R.string.state_rejected;
            background = R.drawable.bg_pill_rejected;
            color = MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnErrorContainer);
        } else {
            label = R.string.state_pending;
            background = R.drawable.bg_pill_pending;
            color = MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnTertiaryContainer);
        }
        view.setText(label);
        view.setBackgroundResource(background);
        view.setTextColor(color);
    }
}
