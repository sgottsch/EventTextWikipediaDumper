#!/bin/sh
datafolder="data/"

cd $datafolder

wget -O redirects_en.ttl.bz2 https://databus.dbpedia.org/dbpedia/generic/redirects/2022.12.01/redirects_lang=en.ttl.bz2
wget -O types_en.ttl.bz2 https://databus.dbpedia.org/dbpedia/mappings/instance-types/2022.12.01/instance-types_inference=transitive_lang=en.ttl.bz2

bunzip2 redirects_en.ttl.bz2
bunzip2 types_en.ttl.bz2

grep ' <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/' types_en.ttl > types_en.csv
sed -i 's/<http:\/\/dbpedia.org\/resource\///g;s/> <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#type> <http:\/\/dbpedia.org\/ontology\// /g;s/> .//g' types_en.csv

rm types_en.ttl

sed 's/^<http:\/\/dbpedia.org\/resource\///g;s/> <http:\/\/dbpedia.org\/ontology\/wikiPageRedirects> <http:\/\/dbpedia.org\/resource\// /g;s/> .//g' redirects_en.ttl > redirects_en.csv
rm redirects_en.ttl


wget https://databus.dbpedia.org/dbpedia/wikidata/sameas-all-wikis/2022.12.01/sameas-all-wikis.ttl.bzip2
bunzip2 sameas-all-wikis.ttl.bzip2
mv sameas-all-wikis.ttl.bzip2.out sameas-all-wikis.ttl

grep '> <http://www.w3.org/2002/07/owl#sameAs> <http://dbpedia.org/resource/' sameas-all-wikis.ttl > sameas-enwiki.ttl
rm sameas-all-wikis.ttl
grep -v '> <http://www.w3.org/2002/07/owl#sameAs> <http://dbpedia.org/resource/Category:' sameas-enwiki.ttl > sameas-enwiki2.ttl
rm sameas-enwiki.ttl
mv sameas-enwiki2.ttl sameas-enwiki.ttl

sed -i 's/> <http:\/\/www.w3.org\/2002\/07\/owl#sameAs> <http:\/\/dbpedia.org\/resource\// /g;s/<http:\/\/wikidata.dbpedia.org\/resource\///g;s/> .//g' sameas-enwiki.ttl


wget -O dbpedia_types_specific.ttl.bzip2 https://databus.dbpedia.org/dbpedia/mappings/instance-types/2022.12.01/instance-types_inference=specific_lang=en.ttl.bzip2
bunzip2 dbpedia_types_specific.ttl.bzip2
mv dbpedia_types_specific.ttl.bzip2.out dbpedia_types_specific.ttl

grep '> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/' dbpedia_types_specific.ttl > dbpedia_types_specific_grep.ttl

rm dbpedia_types_specific.ttl
mv dbpedia_types_specific_grep.ttl dbpedia_types_specific.ttl

sed -i 's/<http:\/\/dbpedia.org\/resource\///g;s/> <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#type> <http:\/\/dbpedia.org\/ontology\// /g;s/> .//g' dbpedia_types_specific.ttl
