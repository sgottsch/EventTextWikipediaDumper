# Wikipedia Event Texts Dumper

Extract paragraphs, sentences, raw text and links from Wikipedia articles about events.

## Dumps Download

Download Wikipedia dump files below "Articles, templates, media/file descriptions, and primary meta-pages." from a selected folder in https://dumps.wikimedia.org/enwiki/ and store their filenames into a file called dump_file_list.txt.

## Configuration

Create a configuration file like the following to state where to store your results, and the languages and dumps to be used for extraction:

```
data_folder	/home/....../data
languages	en
```

## Test the extraction

Run ``anon.subevents.source.wikipedia.mwdumper.articleprocessing.TextExtractorNew.java`` to test the extraction on a single example file.

## Run the extraction

Export the Dumper class (`anon.subevents.source.wikipedia.mwdumper.Dumper`) as Jar (`Dumper.jar`). Run the extraction from the Wikipedia dump files by running the following command (here for Portuguese, replace `pt` with other languages if needed). [GNU parallel](https://www.gnu.org/software/parallel/) is required.

```
nohup parallel -j9 "bzip2 -dc {} | java -jar -Xmx6G -Xss40m Dumper.jar path_to_config_file.txt en" :::: dump_file_list.txt 2> log_dumper.txt
```