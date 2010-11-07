/***************************************************************************
    begin                : aug 01 2010
    copyright            : (C) 2010 by Benoit Valot
    email                : benvalot@gmail.com
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 23 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package asi.val;

public class css_style {

	public css_style(){
		
	}
	
	public String get_css_data(){
		StringBuffer data = new StringBuffer() ;
		data.append("body,div,dl,dt,dd,ul,ol,li,h1,h2,h3,h4,h5,h6,pre,form,fieldset,input,button,textarea,p,blockquote,th,td,hr { margin:0; padding:0; outline:0;}"+"\n");
		data.append("table {border-collapse:collapse;border-spacing:0}"+"\n");
		data.append("body,fieldset,img,abbr,acronym,hr {border:0}"+"\n");
		data.append("address,caption,cite,code,dfn,em,strong,th,var { font-style:normal;font-weight:normal}"+"\n");
		data.append("ol,ul {list-style:none}"+"\n");
		data.append("caption,th {text-align:left}"+"\n");
		data.append("h1,h2,h3,h4,h5,h6 { font-weight:normal}"+"\n");
		data.append("q:before,q:after {content:''}"+"\n");
		data.append("hr  {display:block;height:1px;_margin:-7px 0;color:#808080;background-color:#808080;clear:both}"+"\n");
		data.append("body *  {line-height:1.22em;}"+"\n");
		//ajout des bordures
		data.append("body { padding-left:6px; padding-right:2px;}"+"\n");		
		data.append("input[type=submit],input[type=button] {overflow:visible;_width:1px}"+"\n");
		data.append("button,input.button {overflow:visible;_width:1px}"+"\n");
		data.append("textarea {height:58px;overflow-y:scroll}"+"\n");
		data.append("body {background:#fff}"+"\n");
		data.append("div {text-align:left;background:transparent}"+"\n");
		data.append("a:hover, a:active, ul#onglets-emissions li a, ul#onglets-agenda li a { outline:0;}"+"\n");
		data.append("a:focus {outline:1;}"+"\n");
		
		//link
		data.append("a { text-decoration:none; color:#6c829e; font-weight:bold; font-size:10px;} "+"\n");
		data.append("a:hover { text-decoration:none; color:#000;}"+"\n");
		data.append("a.chevron:hover,a.chevrons:hover { text-decoration:none;background-color:transparent}"+"\n");
		data.append("a.chevron:hover span,a.chevrons:hover span { text-decoration:underline}"+"\n");
		
		 data.append(".bloc-bande-contenu  {background:transparent url(\"/images/blocs/fonds/cont-html-doss-1-bg.png\") left bottom repeat-y;width:737px;border-bottom:1px solid #BEC6D1;border-left:1px solid #BEC6D1;border-right:1px solid #BEC6D1;position:relative;}"+"\n");
		 data.append(".bloc-bande-contenu-chro  {background:transparent url(\"/images/blocs/fonds/cont-html-chro-1-bg.png\") left bottom repeat-y !important;}	"+"\n");
		 data.append(".bloc-bande-contenu-emi  {background:transparent url(\"/images/blocs/fonds/cont-html-emi-1-bg.png\") left bottom repeat-y !important;}"+"\n");
		 data.append("#container-contenu-html  {background:transparent url(\"/images/blocs/fonds/cont-html-doss.png\") bottom left no-repeat;padding:14px 0 0 30px;position:relative;}	"+"\n");
//		 data.append("#container-contenu-html .filet {width:680px;}"+"\n");
		 data.append(".container-contenu-html-chro  {background:transparent url(\"/images/blocs/fonds/cont-html-chro.png\") bottom left no-repeat !important;}"+"\n");
		 data.append(".container-contenu-html-emi  {background:transparent url(\"/images/blocs/fonds/cont-html-emi.png\") bottom left no-repeat !important;}"+"\n");
		 data.append("#titrage-contenu {font-weight:bold;margin-left:-25px;padding-left:25px;padding-right:70px;background:transparent url(\"/images/blocs/fonds/titre-cont-bg.png\") bottom left repeat-x;}"+"\n");	
		 data.append("#titrage-contenu .typo-titre {font-size:20px;font-weight:bold;text-transform:uppercase;}"+"\n");
		 data.append("#titrage-contenu .typo-titre:hover {color:#000;}"+"\n");
		 data.append("#titrage-contenu .typo-sous-titre {font-size:16px;font-weight:bold;text-transform:none;}"+"\n");
		 data.append("#footer-contenu, #footer-contenu .typo-date  {font-size:13px;}"+"\n");
		 data.append("#footer-contenu a {font-size:13px;font-weight:bold;}"+"\n");
		 data.append(".outils-contenu {font-weight:bold;background-color:#fff;min-height:30px;height:30px;margin-left:-25px;padding-left:25px;position:relative;}"+"\n");
		 data.append(".outils-contenu img {vertical-align:middle;margin-right:3px;}"+"\n");
		 data.append(".outils-contenu a {margin-right:16px;}"+"\n");
		 data.append(".contenu-html {padding-top:10px; padding-bottom:20px;font-size:13px;color:#030303;overflow:hidden;text-align:justify;}"+"\n");
		 data.append(".contenu-html td embed, .contenu-html td object, .contenu-html td p {width:320px !important;}"+"\n");
		 data.append(".contenu-html p {margin:0;padding:7px 0 7px 0;line-height:16px!important;}"+"\n");
		 data.append(".contenu-html table {margin:0 0 7px 0}"+"\n");
		 //adapter les images
		 data.append(".contenu-html img {WIDTH:100%;text-align:center;}"+"\n");
		 
		 data.append(".contenu-html td {text-align:justify;vertical-align:top !important;line-height:16px!important;}"+"\n");
		 data.append(".contenu-html em, .contenu-html em * {font-style:italic;line-height:16px!important;}"+"\n");
		 data.append(".contenu-html hr {border-bottom:1px dotted #BEC6D1;height:1px;margin:5px 0 5px 0;padding:0;line-height:1px;font-size:1px;}"+"\n");
		 data.append(".contenu-html strong, .contenu-html strong * {font-weight:bold;line-height:16px!important;}"+"\n");
		 data.append(".contenu-html a {text-decoration:underline;font-size:13px;font-weight:normal;line-height:16px!important;}"+"\n");
		 data.append(".contenu-html u {text-decoration:underline;line-height:16px!important;}"+"\n");
		 data.append(".contenu-html .chapeau, .contenu-html .chapeau * {font-size:14px;color:#030303;font-weight:bold;line-height:17px!important;}"+"\n");
		 data.append(".contenu-html .inter-titre {font-size:14px;color:#030303;font-weight:bold;	text-transform:uppercase;line-height:17px!important;}"+"\n");
		 data.append(".contenu-html .citation, .contenu-html blockquote {font-size:13px;color:#030303;font-style:italic;line-height:17px!important;}"+"\n");
		 data.append(".contenu-html .surlignage-fluo {background-color:#ffe7a3;line-height:16px!important;}"+"\n");
		 data.append(".contenu-html .regardez {font-size:13px;color:#b0b0b0;	font-weight:bold;line-height:16px!important;}"+"\n");
		 data.append(".contenu-html .texte-barre {text-decoration:line-through;}"+"\n");
		 data.append(".contenu-html ul {list-style-image: url(/images/puce.gif);padding:5px 20px 5px 20px;}"+"\n");
		 data.append(".bloc-bande-vite .contenu-html {padding-right:1px;}"+"\n");

		 data.append("#nuage-tags { border:1px solid #AEB7C6; background:#fff url(\"/images/blocs/fonds/mot-cles-bg.png\") bottom left no-repeat; padding:8px; text-align:justify;}"+"\n");
		 data.append(".right-rub { background:#fff url(\"/images/blocs/fonds/mot-cles-bg.png\") bottom left no-repeat;padding:8px;text-align:justify;font-size:11px;border-bottom:1px solid #AEB7C6;border-right:1px solid #AEB7C6;border-left:1px solid #AEB7C6;}"+"\n");
		 data.append(".right-rub-porte { background:#ff398e ;padding:8px;text-align:justify;font-size:11px;border-bottom:1px solid #AEB7C6;border-right:1px solid #AEB7C6;border-left:1px solid #AEB7C6;}"+"\n");
		 data.append(".right-rub a {font-size:11px;}"+"\n");
		 data.append(".typo-mono {font-family:courrier new,monospace;}"+"\n");
		 data.append(".typo-info {color:#999;font-size:10px}"+"\n");
		 data.append(".typo-date {color:#999;font-size:10px;font-weight:bold;}"+"\n");
		 data.append(".typo-heure {color:#999;font-size:30px;font-weight:bold;}"+"\n");
		 data.append(".typo-nombre {color:#6e6e6e;font-weight:bold;font-size:10px;}"+"\n");
		 data.append(".typo-vite-rub {color:#6e6e6e;font-size:14px;font-weight:bold;} "+"\n");
		 data.append(".typo-centre-titre, .typo-dup-titre, .typo-centre-dup-titre {font-size:12px; font-weight:bold; text-transform:uppercase; color:#000;}"+"\n");
		 data.append(".typo-centre-sous-titre {text-transform:none;}"+"\n");
		 data.append(".typo-droite-titre {font-size:11px;font-weight:bold;text-transform:uppercase;color:#000;}"+"\n");
		 data.append(".typo-droite-sous-titre {text-transform:none;}"+"\n");
		 data.append(".typo-droite-titre:hover, .typo-centre-titre:hover, #dossier-bandeau a:hover, #dossier-bandeau {color:#3399ff;}"+"\n");
		 data.append(".typo-chro-titre, .typo-dup-titre {font-size:12px;font-weight:bold;text-transform:uppercase;color:#000;}"+"\n");
		 data.append(".typo-chro-sous-titre { text-transform:none;}"+"\n");
		 data.append(".typo-vite-titre { font-size:20px;font-weight:bold;text-transform:uppercase;color:#000;}"+"\n");
		 data.append(".typo-vite-titre:hover { color:#3399ff;}"+"\n");
		 data.append(".typo-vite-sous-titre { font-size:16px;font-weight:bold;text-transform:none;}"+"\n");
		 data.append(".typo-description { font-size:11px;padding:5px 0 0 0;}"+"\n");
		 data.append(".typo-type-article { font-size:10px;font-weight:bold;color:#3399ff;}"+"\n");
		 data.append(".typo-emi-presente-par { font-size:13.5px; font-weight:bold; color:#aaa; letter-spacing:-0.5px; }"+"\n");
		 data.append(".typo-emi-presente-par strong { color:#000; font-weight:bold;}"+"\n");
		 data.append(".typo-emi-intro { font-size:12px;}"+"\n");
		 data.append(".typo-emi-frequence { font-size:13.5px; font-weight:bold; color:#4e00f9; letter-spacing:-0.5px;}"+"\n");
		 data.append(".typo-titre-page-chro {color:#fff;font-size:16px;text-transform:uppercase;font-weight:bold;margin-left:25px;}"+"\n");
		 data.append(".typo-titre-page-chro:hover {color:#fff}"+"\n");
		 data.append(".typo-titre-page-doss {color:#fff;font-size:16px;text-transform:uppercase;font-weight:bold;margin-left:25px;}"+"\n");
		 data.append(".typo-titre-page-doss:hover {color:#fff}"+"\n");
		 data.append(".typo-titre-page-chro {color:#fff;font-size:16px;text-transform:uppercase;font-weight:bold;margin-left:15px;}"+"\n");
		 data.append(".typo-titre-page-chro:hover {color:#fff}"+"\n");
		 data.append(".typo-titre {color:#000;text-transform:uppercase;}"+"\n");
		 data.append(".typo-sous-titre {font-size:14px;text-transform:none;}"+"\n");
		 data.append(".typo-titre:hover {color:#3399ff;}"+"\n");
		 data.append(".typo-titre img {padding-right:4px;}"+"\n");
		 data.append(".bloc-contenu-8.typo-titre img, .bloc-contenu-10 .typo-titre img {vertical-align:-2px;}"+"\n");
		 data.append(".bloc-contenu-11 .typo-titre img, .bloc-contenu-11 .typo-centre-titre img, .bloc-contenu-9 .typo-titre img {vertical-align:-3px;}"+"\n");

		 data.append(".color-redac {color:#3399ff !important;}"+"\n");
	
		 data.append(".color-chro, .bloc-rech-chro .typo-type-article, .bloc-contenu-1-chro .typo-type-article,.bloc-contenu-1-chro  .typo-titre:hover, .bloc-contenu-2-chro .typo-type-article, .bloc-contenu-2-chro .typo-centre-titre:hover, .bloc-contenu-3 .typo-droite-titre:hover, .bloc-contenu-5-chro .typo-type-article, .bloc-chroniqueur-1 .typo-titre:hover, .bloc-contenu-5-chro .typo-titre:hover, .bloc-contenu-5-chro .typo-sous-titre:hover, .bloc-contenu-6 .typo-titre:hover, .bloc-contenu-6 .typo-sous-titre:hover, .bloc-contenu-chro-7 .typo-droite-titre:hover, .bloc-contenu-chro-7 .typo-type-article, .bloc-contenu-3 .typo-droite-titre:hover, .typo-chro-titre:hover, a.band-chro:hover, span.band-chro, .liens-chro a, .typo-chro, .bloc-contenu-12-chro .typo-titre:hover, .bloc-contenu-12-chro .typo-sous-titre:hover, .bloc-contenu-12-chro .typo-type-article, .bloc-contenu-chro-14 .typo-droite-titre:hover, .bloc-alaune-home-chro .typo-titre:hover, .bloc-alaune-home-chro .typo-type-article, .bloc-gratuit-1-chro .typo-vite-titre:hover, .bloc-agenda-chro .typo-type-article, .bloc-agenda-chro .typo-titre:hover"+"\n"); 
		 data.append("{color:#ff3493 !important;}"+"\n");
	
		 data.append(".color-emi, .bloc-rech-emi .typo-titre:hover, .bloc-contenu-1-emi .typo-type-article,.bloc-contenu-1-emi  .typo-titre:hover, .bloc-contenu-2-emi .typo-type-article, .bloc-contenu-2-emi .typo-centre-titre:hover, .bloc-contenu-5-emi .typo-type-article, .bloc-emission-1 .typo-titre:hover, .bloc-contenu-5-emi .typo-titre:hover, .bloc-contenu-5-emi .typo-sous-titre:hover, .bloc-contenu-emi-7 .typo-droite-titre:hover, .bloc-contenu-emi-7 .typo-type-article, a.band-emi:hover, span.band-emi, .liens-emi a, .typo-emi, .bloc-contenu-8 .typo-titre:hover, .bloc-contenu-8 .typo-sous-titre:hover, .bloc-contenu-8 .typo-type-article, .bloc-contenu-9 .typo-titre:hover, .bloc-contenu-9 .typo-sous-titre:hover, .bloc-contenu-9 .typo-type-article, .bloc-contenu-10 .typo-titre:hover, .bloc-contenu-10 .typo-sous-titre:hover, .bloc-contenu-10 .typo-type-article, .bloc-contenu-11 .typo-centre-titre:hover, .bloc-contenu-11 .typo-centre-sous-titre:hover, .bloc-contenu-11 .typo-type-article, .bloc-contenu-12-emi .typo-titre:hover, .bloc-contenu-12-emi .typo-sous-titre:hover, .bloc-contenu-12-emi .typo-type-article, .bloc-contenu-emi-14 .typo-droite-titre:hover, .bloc-dup-1 .typo-type-article, .bloc-contenu-carrousel .typo-type-article, .bloc-contenu-carrousel .typo-centre-titre:hover, .bloc-dup-emi .typo-type-article, .bloc-dup-emi .typo-dup-titre:hover, .bloc-alaune-home-emi .typo-titre:hover, .bloc-alaune-home-emi .typo-type-article, .bloc-gratuit-1-emi .typo-vite-titre:hover, .bloc-agenda-emi .typo-type-article, .bloc-agenda-emi .typo-titre:hover"+"\n"); 
		 data.append("{color:#3A35FF !important;}"+"\n");

		 data.append(".color-vite, .bloc-vite-contenu-3 .typo-titre:hover, .bloc-contenu-vite-7 .typo-droite-titre:hover, .bloc-contenu-vite-7 .typo-type-article, .bloc-gratuit-1-vite .typo-vite-titre:hover, .bloc-alaune-home-vite .typo-type-article, .bloc-alaune-home-vite .typo-titre:hover, .bloc-vite-gratuit-1 .typo-type-article, .bloc-vite-gratuit-1 .typo-droite-titre:hover, .bloc-vite-gratuit-2 .typo-vite-titre:hover, .bloc-rech-vite .typo-type-article, .bloc-rech-vite .typo-titre:hover, .bloc-contenu-5-vite .typo-type-article, .bloc-contenu-5-vite .typo-titre:hover, .bloc-contenu-5-vite .typo-sous-titre:hover"+"\n");
		 data.append("{color: #fea503 !important;}"+"\n");

		 data.append(".color-forum, .typo-dup-titre:hover, .typo-centre-dup-titre:hover { color:#94c41b;}"+"\n");
		 data.append(".bloc-dup-3 .typo-description { font-weight:normal; color:#000;}"+"\n");
		 data.append(".bloc-dup-3 .typo-description:hover  {font-weight:normal; color:#94c41b;}"+"\n");

		 data.append(".bloc-agenda-gris .typo-type-article, .bloc-agenda-gris .typo-titre:hover"+"\n"); 
		 data.append("{color: #7990a7 !important;}"+"\n");
		
		return(data.toString());
	}
}
