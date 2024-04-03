# Wikipedia Event Texts Dumper

Extract paragraphs, sentences, raw text and links from Wikipedia articles about events.

## Dumps Download

Download Wikipedia dump files below "Articles, templates, media/file descriptions, and primary meta-pages." from a selected folder in [https://dumps.wikimedia.org/enwiki/](https://dumps.wikimedia.org/enwiki/) and store their filenames into a file called dump_file_list.txt. You can start with a single file for testing.

## Other Files Download 

- Run ``sh dbpedia_files_download.sh`` to download relevant files from DBpedia and create the file ``types_en.csv``, ``redirects_en.csv``, ``sameas-enwiki.ttl`` and ``dbpedia_types_specific.ttl``.
- Download the file [instanceof-data.tsv](https://eventkg.l3s.uni-hannover.de/data/instanceof-data.tsv) and run ``de.l3s.wikidumper.source.wikidata.WikidataLinksReducer`` to create ``instanceof-data-relevant.tsv``.

## Configuration

Create a configuration file like the following to state where to store your results, and the languages and dumps to be used for extraction:

```
data_folder	/home/....../data
languages	en
```

## Test the extraction

Run ``de.l3s.wikidumper.source.wikipedia.mwdumper.articleprocessing.TextExtractorNew.java`` to test the extraction on a single example file.

## Run the extraction

Export the Dumper class (`de.l3s.wikidumper.source.wikipedia.mwdumper.Dumper`) as Jar (`Dumper.jar`). Run the extraction from the Wikipedia dump files by running the following command (here for Portuguese, replace `pt` with other languages if needed). [GNU parallel](https://www.gnu.org/software/parallel/) is required.

```
nohup parallel -j9 "bzip2 -dc {} | java -jar -Xmx6G -Xss40m Dumper.jar path_to_config_file.txt en" :::: dump_file_list.txt 2> log_dumper.txt
```