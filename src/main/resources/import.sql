--
-- SET statement_timeout = 0;                                                                                                                                                                                      
-- SET client_encoding = 'UTF8';                                                                                                                                                                                   
-- SET standard_conforming_strings = off;                                                                                                                                                                          
-- SET check_function_bodies = false;                                                                                                                                                                              
-- SET client_min_messages = warning;                                                                                                                                                                              
-- SET escape_string_warning = off;                  
SET search_path = public, pg_catalog;
--
-- Data for Name: appli_config; Type: TABLE DATA; Schema: public; Owner: esupsgc
--
-- postgresql full text search
ALTER TABLE card ADD COLUMN textsearchable_index_col tsvector;
UPDATE card SET textsearchable_index_col = setweight(to_tsvector('simple', coalesce(card.eppn,'')), 'B') || setweight(to_tsvector('simple', replace(coalesce(user_account.name,''),'-',' ')), 'A') || setweight(to_tsvector('simple', replace(coalesce(user_account.firstname,''),'-',' ')), 'B') || setweight(to_tsvector('simple', coalesce(user_account.email,'')), 'B') || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_emp_id,''),'-',' ')), 'B') || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_etu_id,''),'-',' ')), 'B') || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_entite_affectation_principale,''),'-',' ')), 'C')  || setweight(to_tsvector('simple', replace(coalesce(card.csn,''),'-',' ')), 'C') || setweight(to_tsvector('simple', replace(coalesce(card.full_text,''),'-',' ')), 'D') || setweight(to_tsvector('simple', replace(coalesce(user_account.full_text,''),'-',' ')), 'D')  FROM user_account where card.eppn=user_account.eppn;
CREATE INDEX textsearch_idx ON card USING gin(textsearchable_index_col);
CREATE OR REPLACE FUNCTION textsearchable_card_trigger() RETURNS trigger AS $$ begin new.textsearchable_index_col := setweight(to_tsvector('simple', coalesce(new.eppn,'')), 'B') || setweight(to_tsvector('simple', replace(coalesce(user_account.name,''),'-',' ')), 'A') || setweight(to_tsvector('simple', replace(coalesce(user_account.firstname,''),'-',' ')), 'B') || setweight(to_tsvector('simple', coalesce(user_account.email,'')), 'B') || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_emp_id,''),'-',' ')), 'B') || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_etu_id,''),'-',' ')), 'B') || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_entite_affectation_principale,''),'-',' ')), 'C') || setweight(to_tsvector('simple', replace(coalesce(new.csn,''),'-',' ')), 'C') || setweight(to_tsvector('simple', replace(coalesce(new.full_text,''),'-',' ')), 'D') || setweight(to_tsvector('simple', replace(coalesce(user_account.full_text,''),'-',' ')), 'D') FROM user_account where new.eppn=user_account.eppn; return new; end $$ LANGUAGE plpgsql;
CREATE TRIGGER tsvectorupdate BEFORE INSERT OR UPDATE ON card FOR EACH ROW EXECUTE PROCEDURE textsearchable_card_trigger();
CREATE OR REPLACE FUNCTION textsearchable_user_account_trigger() RETURNS trigger AS $$ begin update card set textsearchable_index_col = setweight(to_tsvector('simple', coalesce(card.eppn,'')), 'B') || setweight(to_tsvector('simple', replace(coalesce(new.name,''),'-',' ')), 'A') || setweight(to_tsvector('simple', replace(coalesce(new.firstname,''),'-',' ')), 'B') || setweight(to_tsvector('simple', coalesce(new.email,'')), 'B') || setweight(to_tsvector('simple', replace(coalesce(new.supann_emp_id,''),'-',' ')), 'B') || setweight(to_tsvector('simple', replace(coalesce(new.supann_etu_id,''),'-',' ')), 'B') || setweight(to_tsvector('simple', replace(coalesce(new.supann_entite_affectation_principale,''),'-',' ')), 'C') || setweight(to_tsvector('simple', replace(coalesce(card.csn,''),'-',' ')), 'C') || setweight(to_tsvector('simple', replace(coalesce(card.full_text,''),'-',' ')), 'D') || setweight(to_tsvector('simple', replace(coalesce(new.full_text,''),'-',' ')), 'D') where card.eppn=new.eppn; return new; end $$ LANGUAGE plpgsql;
CREATE TRIGGER tsvectorupdateUser AFTER UPDATE ON user_account FOR EACH ROW EXECUTE PROCEDURE textsearchable_user_account_trigger();
INSERT INTO appli_version (id, esup_sgc_version, version) SELECT nextval('hibernate_sequence'), '2.2.x', '1' WHERE NOT EXISTS (SELECT * FROM appli_version);
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'SYNCHRONIC_EXPORT_CSV_FILE_NAME', 'synchronic.csv', 'Nom du fichier CSV Synchronic', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'TIL_EXPORT_CSV_FILE_NAME', 'til.csv', 'Nom du fichier CSV d''export Til', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'P2S_EXPORT_CSV_NB_LINES_PER_FILE', '1000', 'Nombre de lignes par fichier pour l''export CSV P2S.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'P2S_EXPORT_CSV_FILE_NAME', 'p2s.csv', 'Nom du fichier d''export P2S.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'DISPLAY_FORM_CNIL', 'E;I;P', 'Dans le formulaire de demande de carte, population pour laquelle on affiche l''autorisation de diffusion de photo. E=Etudiant, I=Invité, P=Personnel', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'DISPLAY_FORM_CROUS', 'I;P', 'Dans le formulaire de demande de carte, population pour laquelle on affiche la partie Crous. E=Etudiant, I=Invité, P=Personnel', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'DISPLAY_FORM_ADRESSE', 'I;P', 'Dans le formulaire de demande de carte, population pour laquelle on affiche la partie concernant l''adresse de livraison de carte. E=Etudiant, I=Invité, P=Personnel', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'DISPLAY_FORM_RULES', 'E', 'Dans le formulaire de demande de carte, population pour laquelle on affiche le règlement. E=Etudiant, I=Invité, P=Personnel', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'DEFAULT_DATE_FIN_DROITS', '2049-12-31', 'Date de fin de droits par défaut si cette date est vide dans les infos utilisateur.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'MONTANT_RENOUVELLEMENT', '10', 'Montant en euros du renouvellement de carte.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'DEFAULT_CNOUS_ID_COMPAGNY_RATE', '78', 'Id Société Crous par défaut.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'DEFAULT_CNOUS_ID_RATE', '99', 'Id Cnous par défaut.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'RETENTION_LOGS_DB_DAYS', '360', 'Nombre de jours de rétention des logs. Utilisé lors de la purge.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'MAIL_LISTE_PRINCIPALE', 'sgc@univ-rouen.fr', 'Adresse mail à laquelle sont adressés en copie les mails liés aux changements d''état des cartes.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'MAIL_NO_REPLY', '[Sgc - Léocarte] no-reply-sgc@univ-rouen.fr', 'Adresse ''From'' des mails envoyés à partir de l''application.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'MAIL_SUBJECT_AUTO', 'Gestion de carte ', 'Sujet des mails automatiques de demande ou d''activation/réactivation de carte.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'PAYBOX_MSG_SUCCESS', 'Vous avez effectué un paiement oour renouveler votre carte. Vous pouvez maintenant demander une nouvelle carte en remplissant le formulaire dans votre espace utilsateur.', 'Corps du message envoyé à utilisateur lorsqu''il a effectué un paiement Paybox pour un renouvellement de carte.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_MSG_HELP', '<p>Une fois votre carte éditée, pour signaler tout problème, faites une demande d’assistance sur le <a href=''http://helpetu.univ-rouen.fr'' target="blank">helpetu</a> rubrique léocarte.</p>', 'Message de la vue ''Utilisateur'' indiquant le lien d''assistance concernant les problèmes de cartes.' , 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_MSG_FREE_RENEWAL', '<p>Si votre carte est endommagée, hors service, ou encore volée ou perdue (n''oubliez pas alors de la désactiver dès maintenant) ... vous avez la possibilité de la renouveler gratuitement.</p>', 'Message de l''interface ''Utilisateur'' indiquant les conditions de renouvellement gratuit. N''est affiché que si ces conditions sont remplies.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_MSG_CAN_PAID_RENEWAL', '<p>Pour l''année universitaire courante, vous ne pouvez pas renouveler votre carte gratuitement (le renouvellement gratuit est possible tous les 3 ans). Cependant, si vous avez perdu votre carte par exemple (n''oubliez pas alors de la désactiver dès maintenant), vous avez la possibilité de la renouveler pour la somme de10 €. Vous devez donc payer la somme de 10 € avant de pouvoir accéder au formulaire de demande de carte.</p><p><strong>Ce paiement permet uniquement de faire une nouvelle demande de carte dans le cadre d''un renouvellement avant 3 ans d''usage.</strong>&nbsp;Cela ne crédite d''aucune façon votre carte actuelle.&nbsp;</p>', 'Message de l''interface ''Utilisateur'' indiquant les conditions de renouvellement payant. N''est affiché que si ces conditions sont remplies.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_MSG_PAID_RENEWAL', '<p>Vous avez payé un montant de 10 € pour renouveler votre carte. Vous pouvez maintenant demander une nouvelle carte en remplissant le formulaire qui suit.</p>', 'Message de l''interface ''Utilisateur'' s''affichant si une somme a été payée pour le renouvellement de carte, donnant accès au formulaire de demande.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'ANNEE_UNIV', '2017-07-05','Dates de commencement des années universitaires présente dans le sgc. Utilisés pour les statistiques. Doivent être au format yyyy-MM-dd et classées par ordre coissant , séparé par un / ex: 2017-07-01/2018-057-01/....', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_MSG_FORM_REJECTED', '<p><strong>Votre demande a été rejetée. Merci de la renouveler avec une photo conforme aux recommandations ci-dessous !</strong></p>', 'Message apparaissant sur le formulaire de demande de carte lorsque la précédente demande a été rejetée.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_FORM_RULES', '<p>Votre léocarte vous sera remise pour les étudiants suivant les cours en présentiel soit à l’opération modulo soit en scolarité <a target=''_blank'' href=''http://dsi-10.univ-rouen.fr/user/card-request-form#''>http://modulo.univ-rouen.fr/</a></p><p>Pour les étudiants suivant les cours à distance, une remise de la carte se fera par envoi postal. <a target=''_blank'' href=''http://formation-ve.univ-rouen.fr/leo-kezako--398116.kjsp?RH=1392816765290''>En savoir plus sur les modalités de remise de la léocarte<br></a></p>', 'Message inclus dans le formulaire de demande de carte concernant la remise de la carte.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_FREE_FORCED_RENEWAL', '<p>Vous pouvez donner le droit à cet étudiant de refaire gratuitement sa carte.<br></p>', 'Message s''affichant pour ceux se connectant en ''SU'' sur la vue ''Utilisateur'', associé au bouton permettant de forcer le renouvellement de carte gratuit.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_TIP_MSG', '<p>Pour chacune de vos cartes, vous avez la possibilité d''invalider votre vous-même en renseignant la raison de cette invalidation (volée, perdue, endommagée). <br>Vous pourrez faire également l''opération inverse sans justification (cad réactiver une carte perdue que vous auriez finalement retrouvée par exemple).</p>', 'Message informatif de la vue ''Utilisateur''.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'HELP_MANAGER', '<p><h3>Aide<br></h3><h4>Recherche<br></h4><p>La recherche libre s''effectue par défaut sur le nom, prénom, email. Vous pouvez ajouter des filtres à l''aide des listes déroulantes. En cliquant sur le bouton rouge, vous effectuer une recherche sans filtre.</p><h4>CSV<br></h4><p>Vous pouvez sélectionner les champs et télécharger les résultats de la recherche effectuée en CSV.</p><h4>Resynchronisation<br></h4><p>Resynchronise les infos à partir des différentes sources de données.</p><p><span style="background-color: rgb(217, 237, 247); color: inherit; font-family: inherit; font-size: 18px;">Apps</span></p><p>Le menu Apps vous donne accès à une application Android ou encore de bureau en Java.</p><p>Application Android : application pour téléphone Android disposant d''une connexion internet et supportant le NFC</p><p>Application Java : application pour ordinateur disposant de Java, d''une connexion internet et d''un lecteur NFC (USB)</p><p>Ces applications permettent de ''badger'' les cartes. En fonction de vos droits, ce badgeage peut servir à marque une carte comme livrée ou bien rechercher une carte simplement.</p><p>Le badgeage de carte dans sa fonctionnalité de recherche permet d''afficher la page de l''utilisateur/carte dans l''interface web ESUP-SGC dans laquelle vous êtes connecté !</p>','Bloc d''aide s''affichant dans le menu ''Manager''', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'HELP_USER','<h3>Aide<br></h3><h4>Utilisateur<br></h4><p>Vous pouvez modifier à tout moment la permission concernant la diffusion de votre photo pour un usage interne.</p><br><h4>Cartes</h4><p>L''utilisateur voit toutes les cartes qui lui ont été attribuées.<br></p><p>Pour une nouvelle demande, tant que carte n''est pas activée vous pouvez connaitre l’état de votre carte.</p><p>A tout moment vous pouvez désactiver/réactiver votre carte.</p><br>', 'Bloc d''aide s''affichant dans le menu utilisateur lorsque l''on clique sur le bouton ''Aide'' du menu haut.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'HELP_ADMIN', '<h3>Aide<br></h3><h4>Sessions<br></h4><p>Nombre de sessions en cours sur esup-sgc ainsi que le type d''utilisateur.<br></p><h4>Configurations</h4><p>Différentes configurations stockées en base de données.<br></p><h4>Esup-nfc</h4><h4>Logs</h4><p>Logs remontés par les actions prinicipales&nbsp; des utilisateurs d''esup-sgc<br></p><h4>Transactions Paybox</h4><p>Liste des transactions paybox.<br></p><h4>Crous</h4><p>Liste de donées CROUS/IZLY. Possibilté d''import.<br></p><h4>Import</h4><p>Import CSV</p>', 'Bloc d''aide s''affichant dans le menu ''Admin''', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'ENABLE_AUTO', 'true', 'Si true, la carte se met directement à l''état ''ENABLED'' (activé) à la fin de l''encodage, sinon la carte se met à l''état ''ENCODED'' (encodé).', 'BOOLEAN');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'QRCODE_ESC_ENABLED', 'false', 'Si ''false'', le QRCODE contient l''eppn. Si ''true'', il contient l''identifiant dans le cadre de la carte européenne', 'BOOLEAN');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'QRCODE_FORMAT', 'PNG', 'Format de l''image du QRCODE utilisé pour l''impression', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'EXT_USER_EPPN_REGEXP', '.*@univ-paris1.fr\n.*@insa-rouen.fr', ' Regexp sur l''eppn, ceux qui matchent peuvent demander une carte.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'MODE_LIVRAISON', 'true', 'Affiche ou non ce qui concerne la livraison', 'BOOLEAN');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_MSG_NEW_CARD', '<p>Nous accusons réception de votre demande de Léocarte. Celle-ci sera traitée dans les meilleurs délais (hors période du 21/07 au 21/08).</p>', 'Message s''affichant dans la vue ''Utilisateur'' lorsque la demande est à l''état ''NEW''.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_MSG_CHECKED_OR_ENCODED_CARD', '<p>Votre demande est prise en compte. Votre carte est en cours d''édition..</p>', 'Affiche ou non ce qui concerne les borne carte', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_MSG_REJECTED_CARD','<p><p>Merci de renouveler la demande de Léocarte à partir de votre ENT rubrique Léoservices car la photo n''est pas conforme pour une des raisons suivantes :</p></p><ul><li>photo tronquée</li><li>photo de profil</li><li>photo de mauvaise qualité</li><li>accessoires non autorisés</li></ul><p><p>L''équivalent d''une photo d''identité est attendu. Un outil de prévisualisation de la carte est disponible lors de la demande. </p></p>' , 'Message s''affichant dans la vue ''utilisateur'' lorsque la demande de carte a été rejetée.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_MSG_ENABLED_CARD', '<p>Bonjour,</p><br><p>L''édition de votre Léocarte est terminée.&nbsp;</p><br><p>Modalités de remise de la Léocarte :&nbsp;</p><br><p>--Etudiants--</p><p>Pour ceux s''inscrivant pour la première fois dans l''établissement, cette remise se fera lors des journées modulo à la rentrée. A l''issue de ces journées, les cartes seront disponibles en scolarité (CPGE : envoi à domicile). Pour les demandes de renouvellement, les cartes seront disponibles en scolarité également.</p><br><p>-- Personnels et Invités --</p><p>Les cartes seront remises à votre structure de rattachement ou à l''adresse de livraison indiquée lors de la demande de celle-ci.</p><br><p>Cordialement</p><br>', 'Message s''affichant pour les étudiants dans la vue ''Utilisateur'' quand la carte est la carte est à l''état ''ENABLED'' et que la carte n''est pas livrée.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'USER_MSG_ENABLED_PERS_CARD', '<p>L''édition de votre Léocarte est terminée. Modalités de remise de la Léocarte : Les cartes seront remises à votre structure de rattachement ou sur l''adresse de livraison indiquée (selon demande) en début d''année scolaire.</p><p>Cordialement</p>', 'Message s''affichant pour les non-étudiants dans la vue ''Utilisateur'' quand la carte est la carte est à l''état ''ENABLED'' et que la carte n''est pas livrée.', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'ENABLE_CROUS', 'E', 'Dans l''application le champ ''crous'' est par défaut à ''false''. En renseignant une ou plusieurs poupulations, le champ crous sera à ''true'' pour celle(s)-ci. E=Etudiant, I=Invité, P=Personnel.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'ENABLE_EUROPEAN_CARD', '', 'Dans l''application le champ ''europeanTransient'' est par défaut à ''false''. En renseignant une ou plusieurs poupulations, le champ ''europeanTransient'' sera modifié à ''true'' pour celle(s)-ci. E=Etudiant, I=Invité, P=Personnel.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'DISPLAY_FORM_EUROPEAN_CARD', '', 'Dans le formulaire de demande de carte, population pour laquelle on affiche la partie ''Carte Européenne''. E=Etudiant, I=Invité, P=Personnel', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'PAGE_FOOTER', 'Esup-sgc', 'Texte du pied de page de l''application', 'HTML');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'PHOTO_SIZE_MAX', '200000', 'Taille maximale (en octets) de la photo que l''on peut télécharger lors de la demande de carte', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'PHOTO_BORDEREAU', 'true', 'Affiche ou non la photo dans le bordereau', 'BOOLEAN');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'PAIEMENT_ALERT_MAILTO', '', 'Adresse mail à laquelle sont adressés les mails alertant d''un paiement paybox', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'PAIEMENT_ALERT_MAILBODY', '', 'Contenu du mail alertant un paiement paybox', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'CROUS_INE_AS_IDENTIFIER', 'true', 'Si true, l''INE / supannCodeINE est utilisé comme identifiant crous/izly quand celui-ci est renseigné, si false ou si le supannCodeINE n''est pas renseigné on utilise l''EPPN.', 'BOOLEAN');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'ESUP_SGC_ETABLISSEMENT_NAME', '', 'Nom de votre ESUP-SGC : sert notamment à la construction du user-agent utilisé dans les requêtes REST (les caractères spéciaux y seront ignorés).', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'BMP_COMMAND_COLOR_PRINTER', 'wget -4 ''http://localhost:8080/wsrest/view/%s/card-b64.html?type=color'' -O card-b64.html && chromium --headless --disable-gpu --print-to-pdf=card.pdf card-b64.html && convert -resize 1016x648 -gravity center -extent 1016x648 -density 600 -alpha off card.pdf card.bmp', 'Commande permettant de récupérer un fichier card.bmp présentant le BMP couleur de la carte à imprimer. Utilisé lors de l''impression+encodage en 1 temps. Cette commande est exécutée dans un répertoire temporaire créé à la demande et à chaque appel par esup-sgc', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'BMP_COMMAND_BLACK_PRINTER', 'wget -4 ''http://localhost:8080/wsrest/view/%s/card-b64.html?type=black'' -O card-b64.html && chromium --headless --disable-gpu --print-to-pdf=card.pdf card-b64.html && gs -o card-resize.pdf -sDEVICE=pdfwrite -dPDFFitPage -g10160x6480 card.pdf && convert -monochrome card-resize.pdf card.bmp', 'Commande permettant de récupérer un fichier card.bmp présentant le BMP noir et blanc de la carte à imprimer. Utilisé lors de l''impression+encodage en 1 temps. Cette commande est exécutée dans un répertoire temporaire créé à la demande et à chaque appel par esup-sgc', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'BMP_COMMAND_VIRTUAL', 'wget -4 ''http://localhost:8080/wsrest/view/%s/card-b64.html?type=virtual'' -O card-b64.html && chromium --headless --disable-gpu --print-to-pdf=card.pdf card-b64.html && convert -resize 1016x648 -gravity center -extent 1016x648 -density 600 -alpha off card.pdf card.bmp', 'Commande permettant de récupérer un fichier card.bmp présentant le BMP complet de la carte. Utilisé par l''utilisateur pour afficher sa carte sur mobile par exemple.', 'TEXT');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'PRINTER_ROLE_CONFIG', 'true', 'En plus d''être MANAGER, le ROLE_PRINTER (ou l''affectation à une imprimante via eppn ou groupe pour l''édition en 1 passe) est requis pour pouvoir imprimer une carte.', 'BOOLEAN');
INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/esupnfctagdroid.svg', 'Lecteur NFC pour Android (apk)', 'https://play.google.com/store/apps/details?id=org.esupportail.esupnfctagdroid', 3, 5);
INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/qrcode.svg', 'Installateur Win64 des clients ESUP-SGC', '/esup-sgc-client-installer.zip', 3, 0);
INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/esupnfctagdesktop.svg', 'Lecteur NFC pour Desktop (jar)', 'https://esup-nfc-tag.univ-ville.fr/nfc-index/download-jar', 2, 6);
INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/esupnfctagkeyboard.svg', 'Lecteur NFC pour emulation clavier (jar)', 'https://esup-nfc-tag.univ-ville.fr/nfc-index/download-keyb', 3, 7);
INSERT INTO nav_bar_app_visible4role (SELECT nav_bar_app.id, 'CONSULT' FROM nav_bar_app);
INSERT INTO nav_bar_app_visible4role (SELECT nav_bar_app.id, 'UPDATER' FROM nav_bar_app);
INSERT INTO nav_bar_app_visible4role (SELECT nav_bar_app.id, 'VERSO' FROM nav_bar_app);
INSERT INTO nav_bar_app_visible4role (SELECT nav_bar_app.id, 'LIVREUR' FROM nav_bar_app);
INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/zebra-1.svg', 'Encodeur - robot ZXP3 [Java]', '/esupsgcclient-r2d2-shib.jar', 0, 4);
INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/qrcode.svg', 'Encodeur [Java]', '/esupsgcclient-shib.jar', 1, 3);
INSERT INTO nav_bar_app_visible4role (SELECT nav_bar_app.id, 'MANAGER' FROM nav_bar_app);


