#!/bin/sh
datafolder="data/"

cd $datafolder

wget -O redirects_en.ttl.bz2 https://databus.dbpedia.org/dbpedia/generic/redirects/2021.06.01/redirects_lang=en.ttl.bz2
wget -O types_en.ttl.bz2 https://databus.dbpedia.org/dbpedia/mappings/instance-types/2021.03.01/instance-types_inference=transitive_lang=en.ttl.bzip2

bunzip2 redirects_en.ttl.bz2
bunzip2 types_en.ttl.bz2

grep ' <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/' types_en.ttl > types_en.csv
sed -i 's/<http:\/\/dbpedia.org\/resource\///g;s/> <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#type> <http:\/\/dbpedia.org\/ontology\// /g;s/> .//g' types_en.csv

rm types_en.ttl

sed 's/^<http:\/\/dbpedia.org\/resource\///g;s/> <http:\/\/dbpedia.org\/ontology\/wikiPageRedirects> <http:\/\/dbpedia.org\/resource\// /g;s/> .//g' redirects_en.ttl > redirects_en.csv
rm redirects_en.ttl
