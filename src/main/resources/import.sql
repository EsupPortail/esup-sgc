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
UPDATE card SET textsearchable_index_col = 
     setweight(to_tsvector('simple', coalesce(card.eppn,'')), 'B') 
     || setweight(to_tsvector('simple', replace(coalesce(user_account.name,''),'-',' ')), 'A') 
     || setweight(to_tsvector('simple', replace(coalesce(user_account.firstname,''),'-',' ')), 'B') 
     || setweight(to_tsvector('simple', coalesce(user_account.email,'')), 'B') 
     || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_emp_id,''),'-',' ')), 'B') 
     || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_etu_id,''),'-',' ')), 'B') 
     || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_entite_affectation_principale,''),'-',' ')), 'C') 
     || setweight(to_tsvector('simple', replace(coalesce(card.csn,''),'-',' ')), 'C') 
     FROM user_account where card.eppn=user_account.eppn;
CREATE INDEX textsearch_idx ON card USING gin(textsearchable_index_col);


CREATE FUNCTION textsearchable_card_trigger() RETURNS trigger AS $$
begin
  new.textsearchable_index_col :=
     setweight(to_tsvector('simple', coalesce(new.eppn,'')), 'B') 
     || setweight(to_tsvector('simple', replace(coalesce(user_account.name,''),'-',' ')), 'A') 
     || setweight(to_tsvector('simple', replace(coalesce(user_account.firstname,''),'-',' ')), 'B') 
     || setweight(to_tsvector('simple', coalesce(user_account.email,'')), 'B') 
     || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_emp_id,''),'-',' ')), 'B') 
     || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_etu_id,''),'-',' ')), 'B') 
     || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_entite_affectation_principale,''),'-',' ')), 'C') 
     || setweight(to_tsvector('simple', replace(coalesce(new.csn,''),'-',' ')), 'C') 
     FROM user_account where new.eppn=user_account.eppn;
  return new;
end
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate BEFORE INSERT OR UPDATE
    ON card FOR EACH ROW EXECUTE PROCEDURE textsearchable_card_trigger();

INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'SYNCHRONIC_EXPORT_CSV_FILE_NAME', 'synchronic.csv');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'TIL_EXPORT_CSV_FILE_NAME', 'til.csv');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'P2S_EXPORT_CSV_NB_LINES_PER_FILE', '1000');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'P2S_EXPORT_CSV_FILE_NAME', 'p2s.csv');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'DISPLAY_FORM_CNIL', 'EIP');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'DISPLAY_FORM_CROUS', 'IP');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'DISPLAY_FORM_ADRESSE', 'IP');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'DISPLAY_FORM_RULES', 'E');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'DEFAULT_DATE_FIN_DROITS', '2049-12-31');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'MONTANT_RENOUVELLEMENT', '10');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'DEFAULT_CNOUS_ID_COMPAGNY_RATE', '78');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'DEFAULT_CNOUS_ID_RATE', '99');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'RETENTION_LOGS_DB_DAYS', '360');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'MAIL_LISTE_PRINCIPALE', 'sgc@univ-ville.fr');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'MAIL_NO_REPLY', '[Sgc - carte] no-reply-sgc@univ-ville.fr');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'MAIL_SUBJECT_AUTO', 'Gestion de carte ');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'PAYBOX_MSG_SUCCESS', 'Vous avez effectué un paiement oour renouveler votre carte. Vous pouvez maintenant demander une nouvelle carte en remplissant le formulaire dans votre espace utilsateur.');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'USER_MSG_HELP', 'Une fois votre carte éditée, pour signaler tout problème, faites une demande d’assistance sur le <a  href="https://helpdesk.univ-ville.fr" target="_blank"><strong>helpdesk</strong></a> rubrique carte.');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'USER_MSG_FREE_RENEWAL', 'Si votre carte est endommagée, hors service, ou encore volée ou perdue (noubliez pas alors de la désactiver dès maintenant) ... vous avez la possibilité de la renouveler gratuitement.');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'USER_MSG_CAN_PAID_RENEWAL', 'Pour l année universitaire courante, vous ne pouvez pas renouveler votre carte gratuitement (le renouvellement gratuit est possible tous les 3 ans).</p><p>Cependant, si vous avez perdu votre carte par exemple (noubliez pas alors de la désactiver dès maintenant), vous avez la possibilité de la renouveler pour la somme de 10 €.</p>
<p>Vous devez donc payer la somme de 10 € avant de pouvoir accéder au formulaire de demande de carte.</p>');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'USER_MSG_PAID_RENEWAL', '<p>Vous avez payé un montant de ${montant} € pour renouveler votre carte. Vous pouvez maintenant demander une nouvelle carte en remplissant le formulaire qui suit.</p>');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'USER_MSG_DELIVERED_CARD', 'Modalités de remise de la Léocarte :Pour les étudiants sinscrivant pour la première fois dans létablissement, cette remise se fera lors des journées modulo à la rentrée. En savoir plus  A lissue de ces journées les cartes seront disponibles en scolarité (hors CPGE)
Pour les demandes de renouvellement, les cartes seront disponibles en scolarité également.');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'USER_MSG_EDITED_CARD', 'Important : lédition de carte étudiante nest possible quà partir du moment où :');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'ANNEE_UNIV', '2017-07-05');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'USER_MSG_FORM_REJECTED', 'Votre demande a été rejetée. La raison peut être que la photo affichée sur cette page et que vous avez fourni à l origine, nest pas conforme aux recommandations ci-dessous. Veuillez renouveler votre demande avec une autre photo conforme.');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'USER_FORM_RULES', 'Pour les étudiants suivant les cours à distance, une remise de la carte se fera par envoi postal.');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'USER_FREE_FORCED_RENEWAL', 'Vous pouvez donner le droit à cet étudiant de refaire gratuitement sa carte.');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'USER_TIP_MSG', 'Pour chacune de vos cartes, vous avez la possibilité dinvalider votre vous-même en renseignant la raison de cette invalidation (volée, perdue, endommagée).
Vous pourrez faire également lopération inverse sans justification (cad réactiver une carte perdue que vous auriez finalement retrouvée par exemple).');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'STATS_BANNED_IP', '127.0.0.1');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'HELP_MANAGER', 'help!');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'HELP_USER', 'help!');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'HELP_ADMIN', 'help!');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'ENABLE_AUTO', 'false');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'QRCODE_ESC_ENABLED', 'false');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'QRCODE_WIDTH', '1.2cm');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'QRCODE_FORMAT', 'PNG');
INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'EXT_USER_EPPN_REGEXP', '', ' Regexp sur l''eppn, ceux qui matchent peuvent demander une carte.', 'TEXT');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'MODE_LIVRAISON', 'false');
INSERT INTO appli_config (id, key, value) VALUES (nextval('hibernate_sequence'), 'MODE_BORNES', 'false');
