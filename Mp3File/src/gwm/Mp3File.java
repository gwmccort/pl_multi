package gwm;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * MP3 File, gets info from the mp3 tag.
 *
 * @author gwmccort
 *
 */
public class Mp3File {
	File file;
	String title;
	String artist;
	String albumArtist;
	String album;

	/**
	 * Create a new Mp3File object
	 *
	 * @param file
	 *            that contains mp3 file
	 * @throws Exception
	 */
	public Mp3File(File file) throws Exception { // TODO: deal w/ exception
		this.file = file;
		AudioFile af = AudioFileIO.read(file);
		Tag tag = af.getTag();

		this.title = tag.getFirst(FieldKey.TITLE);
		this.artist = tag.getFirst(FieldKey.ARTIST);
		this.albumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST);
		this.album = tag.getFirst(FieldKey.ALBUM);
	}

	@Override
	public String toString() {
		return "Mp3File [path=" + file.getAbsolutePath() + ", title=" + title
				+ ", artist=" + artist + ", albumArtist=" + albumArtist
				+ ", album=" + album + "]";
	}

	/**
	 * Create a string array with mp3 file attributes
	 *
	 * @return String[] of mp3 file attributes
	 */
	public String[] toArray() {
		String[] sa = new String[5];
		sa[0] = albumArtist;
		sa[1] = artist;
		sa[2] = album;
		sa[3] = title;
		sa[4] = file.toString();
		return sa;
	}

	/**
	 * Write mp3 file attributes as a csv file entry
	 *
	 * @param writer
	 *            CSV file writer
	 */
	public void toCSV(CSVWriter writer) {
		writer.writeNext(toArray());
	}

	/**
	 * Index mp3 file attributes into a Lucene database
	 *
	 * @param writer
	 * @throws Exception
	 */
	public void index(IndexWriter writer) throws Exception {
		Document doc = new Document();
		doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		doc.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("artist", artist, Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field("albumArtist", albumArtist, Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field("album", album, Field.Store.YES, Field.Index.ANALYZED));
		writer.addDocument(doc);
	}

	/**
	 * Index mp3 files in a directory tree
	 *
	 * @param path
	 *            to index
	 * @param writer
	 *            Lucene index writes
	 * @throws Exception
	 */
	public static void indexPath(File path, IndexWriter writer)
			throws Exception {

		// TODO: change to not use file filter
		// IOFileFilter ff = FileFilterUtils.fileFileFilter();
		// IOFileFilter javaSuffix = FileFilterUtils.suffixFileFilter(".mp3");
		// IOFileFilter fileFilter = FileFilterUtils.and(ff, javaSuffix);
		// Collection<File> files = FileUtils.listFiles(path, fileFilter,
		// TrueFileFilter.INSTANCE);
		Collection<File> files = FileUtils.listFiles(path,
				new String[] { "mp3" }, true);
		for (File f : files) {
			Mp3File mp3 = new Mp3File(f);
			mp3.index(writer);
		}
	}

	public static void indexPathTest(String[] args) throws Exception {
		String indexPath = "index";
		Directory dir = FSDirectory.open(new File(indexPath));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31,
				analyzer);
		// iwc.setOpenMode(OpenMode.CREATE);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(dir, iwc);
		Mp3File.indexPath(new File("C:\\Users\\Public\\Music\\Sample Music"),
				writer);
		writer.close();
	}

	public static void singleFileTest(String[] args) throws Exception {
		File file = new File(
				"C:\\Users\\Public\\Music\\Sample Music\\Sleep Away.mp3");
		Mp3File mp3 = new Mp3File(file);
		System.out.println(mp3);
	}

	public static void main(String[] args) throws Exception {
		// disable jul logging output
		Logger globalLogger = Logger.getLogger("");
		Handler[] handlers = globalLogger.getHandlers();
		for (Handler handler : handlers) {
			globalLogger.removeHandler(handler);
		}

		CSVWriter writer = new CSVWriter(new FileWriter("deleteMe.csv"));
		File path = new File("C:\\Users\\Public\\Music");
		Collection<File> files = FileUtils.listFiles(path,
				new String[] { "mp3" }, true);
		System.out.println("files.size:" + files.size());
		for (File f : files) {
			Mp3File mp3 = new Mp3File(f);
			mp3.toCSV(writer);
		}
		writer.close();
	}

}