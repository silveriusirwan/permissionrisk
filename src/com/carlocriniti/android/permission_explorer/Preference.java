/*
 * Projet 	: Permission Explorer
 * Auteur 	: Carlo Criniti
 * Date   	: 2011.06.10
 * 
 * Classe Preference
 * Activit� d'affichage des pr�f�rences
 */
package com.carlocriniti.android.permission_explorer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preference extends PreferenceActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cr�ation de l'interface de pr�f�rences
        addPreferencesFromResource(R.xml.preference);
    }
}
