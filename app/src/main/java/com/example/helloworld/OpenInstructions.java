package com.example.helloworld;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

public class OpenInstructions {

    private final Context context;

    public OpenInstructions(Context context) {
        this.context = context;
    }

    public void show() {
        // Créer une boîte de dialogue
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_instructions);
        dialog.setTitle("Instructions");

        // Récupérer le TextView
        TextView tvInstructions = dialog.findViewById(R.id.tv_instructions);

        // Ajouter le texte formaté
        String instructions =
                "<div style='text-align:center;'>" +
                        "<span style='font-size:24px; font-weight:bold; color:#e7c84d;'><b><big>Instructions</big><b></span>" +
                        "</div>" +
                        "<div>" +
                        "<img src='racket' style='vertical-align:middle; margin-right:10px;'/>" +
                        "<span style='font-size:22px; font-weight:bold; color:#073067;'><b>1. Raquet Command<b></span><br>" +
                        "<span style='font-size:20px; color:#ffffff;'>Move the racket horizontally with your finger to return the ball.</span>" +
                        "</div><br>" +
                        "<div>" +
                        "<img src='bricks' style='vertical-align:middle; margin-right:10px;'/>" +
                        "<span style='font-size:22px; font-weight:bold; color:#073067;'><b>2. Break the bricks<b></span><br>" +
                        "<span style='font-size:20px; color:#ffffff;'>Break all the bricks to get points.</span>" +
                        "</div><br>" +
                        "<div>" +
                        "<img src='malus' style='vertical-align:middle; margin-right:10px;'/>" +
                        "<span style='font-size:22px; font-weight:bold; color:#073067;'><b>3. Bonus<b></span><br>" +
                        "<span style='font-size:20px; color:#ffffff;'>Escape the maluses to avoid reducing the racket size's .</span>" +
                        "</div><br>" +
                        "<div>" +
                        "<img src='life' style='vertical-align:middle; margin-right:10px;'/>" +
                        "<span style='font-size:22px; font-weight:bold; color:#073067;'><b>4. Lives<b></span><br>" +
                        "<span style='font-size:20px; color:#ffffff;'>Every time the ball falls under the racket you lose a life.</span>" +
                        "</div>";

        // Utiliser Html.ImageGetter pour insérer des images
        Html.ImageGetter imageGetter = source -> {
            int resId = 0;
            switch (source) {
                case "racket":
                    resId = R.drawable.racket; // Remplacez par le nom de votre image
                    break;
                case "bricks":
                    resId = R.drawable.bricks;
                    break;
                case "malus":
                    resId = R.drawable.malus_raquette;
                    break;
                case "life":
                    resId = R.drawable.life;
                    break;
            }

            if (resId != 0) {
                Drawable drawable = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    drawable = context.getResources().getDrawable(resId, null);
                }
                drawable.setBounds(0, 0, 100, 100); // Ajustez la taille de l'image
                return drawable;
            }
            return null;
        };
        tvInstructions.setText(HtmlCompat.fromHtml(instructions, HtmlCompat.FROM_HTML_MODE_LEGACY,imageGetter,null));

        dialog.findViewById(R.id.btn_close).setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.6), // Largeur : 90% de l'écran
                    (int) (context.getResources().getDisplayMetrics().heightPixels * 0.6) // Hauteur : 80% de l'écran
            );
        }
    }
}

