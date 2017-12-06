![EsupSgc](https://github.com/EsupPortail/esup-sgc/raw/master/src/main/webapp/images/logo-esup-sgc.png)

Service de Gestion de Cartes - EsupPortail
============================

Cette application permet de gérer le cycle de vie des cartes NFC de votre établissement, de la demande à sa désactivation en passant par son impression, encodage et activation dans votre système d'information.

Cette application fonctionne via une authentification/identification Shibboleth et en lien avec une instance esup-nfc-tag-server

L'ensemble des données est stocké dans une base de données, photos comprises, cela nous a ammené à utiliser PostgreSQL (et non MySQL) pour ses possibilités de streaming sur les blobs. 


## Installation 

### Pré-requis
* Java (JDK - JAVA SE 8):  http://www.oracle.com/technetwork/java/javase/downloads/index.html
* Maven (dernière version 3.0.x) : http://maven.apache.org/download.cgi
* Postgresql 9 : le mieux est de l'installer via le système de paquets de votre linux.
* Tomcat (Tomcat 8)
* Apache + libapache2-mod-shib2 : https://services.renater.fr/federation/docs/installation/sp
* Git

### PostgreSQL
* pg_hba.conf : ajout de 

``` 
host    all             all             127.0.0.1/32            password
``` 

* redémarrage de postgresql
* su postgres
* psql

```
create database esupsgc;
create USER esupsgc with password 'esup';
grant ALL ON DATABASE esupsgc to esupsgc;
```

### Paramétrage mémoire JVM :

Pensez à paramétrer les espaces mémoire JVM : 
```
export JAVA_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256m"
```

Pour maven :
```
export MAVEN_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256m"
```

### Recupération des sources

```
git clone https://github.com/EsupPortail/esup-sgc
```

### Obtention du war pour déploiement sur tomcat ou autre :
```
mvn clean package
```

## Configurations 

### Configurations Systèmes

Logs : 
* src/main/resources/log4j.properties

Base de données : 
* src/main/resources/META-INF/spring/database.properties pour paramètres de connexion
* src/main/resources/META-INF/persistence.xml pour passage de create à update après premier lancement (création + initialisation de la base de données)

Mails : 
* src/main/resources/META-INF/spring/email.properties

### Configuration Apache Shibboleth 


```
   <Location />
     AuthType shibboleth
     ShibRequestSetting requireSession 1
     require shib-session
     ShibUseHeaders On
   </Location>

   <Location "/resources">
   	Require all granted	
	ShibRequireSession Off
   </Location>

   <Location "/wsrest">
	Require all granted
	ShibRequireSession Off
   </Location>

   <Location "/payboxcallback">
	Require all granted
	ShibRequireSession Off
   </Location>

```


## POSTGRESQL

Cette application a été dévelopée en utilisant Spring ROO et donc ses technologies associées.

Comme annoncé ci-dessus, l'application a cependant été développée avec PostgreSQL : lecture/écriture des blobs dans une transaction par streaming ; idexation postgresql (usage de tsvector/tsquery).

Pour une bonne gestion des blob de cette application, il faut ajouter dans PostgreSQL un trigger sur la base de données sur la table big_file.
La fonction lo_manage est nécessaire ici.

Sous debian : 
```
apt-get install postgresql-contrib
```

Puis la création de l'extension lo se fait via un super-user:

* avec postgresql 9 :
```
psql
\c esupsgc
CREATE EXTENSION lo;
```
--

Et enfin ajout du trigger* : 
```
CREATE TRIGGER t_big_file BEFORE UPDATE OR DELETE ON big_file  FOR EACH ROW EXECUTE PROCEDURE lo_manage(binary_file);
```

CF https://www.postgresql.org/docs/9.4/static/lo.html

\* afin que les tables soient préalablement créées, notamment la table big_file sur lequel on souhaite mettre le trigger lo_manage, vous devez démarrer l'application une fois ; en n'oubliant pas ensuite, pour ne pas écraser la base au redémarrage, de __modifier src/main/resources/META-INF/persistence.xml : create-> update__ - cf ci-dessous.

# backup / restauration : 
Avec l'utilisateur postgres
backup : pg_dump -b -F d -f /backup/esupsgc-dump esupsgc
restauration : pg_restore -d esupsgc /backup/esupsgc-dump

## Screenshots
...
