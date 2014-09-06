package com.tngtech.jgiven.report;

import static com.tngtech.jgiven.report.ReportGenerator.Format.HTML;
import static com.tngtech.jgiven.report.ReportGenerator.Format.TEXT;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.tngtech.jgiven.report.html.HtmlReportGenerator;
import com.tngtech.jgiven.report.html.SingleFileHtmlReportGenerator;
import com.tngtech.jgiven.report.html.StaticHtmlReportGenerator;
import com.tngtech.jgiven.report.text.PlainTextReportGenerator;

public class ReportGenerator {

    private static final Logger log = LoggerFactory.getLogger( HtmlReportGenerator.class );

    public enum Format {
        HTML( "html" ),
        TEXT( "text" ),
        GHERKIN( "gherkin" );

        private final String text;

        Format( String text ) {
            this.text = text;
        }

        public static Format fromStringOrNull( String value ) {
            for( Format format : values() ) {
                if( format.text.equalsIgnoreCase( value ) ) {
                    return format;
                }
            }
            return null;
        }
    }

    private File sourceDir = new File( "." );
    private File toDir = new File( "." );
    private File customCssFile = null;
    private Format format = HTML;

    public static void main( String... args ) throws IOException {
        ReportGenerator generator = new ReportGenerator();
        parseArgs( generator, args );
        generator.generate();
    }

    static void parseArgs( ReportGenerator generator, String... args ) {
        for( String arg : args ) {
            if( arg.equals( "-h" ) || arg.equals( "--help" ) ) {
                printUsageAndExit();
            } else if( arg.startsWith( "--dir=" ) ) {
                generator.setSourceDir( new File( arg.split( "=" )[1] ) );
            } else if( arg.startsWith( "--todir=" ) ) {
                generator.setToDir( new File( arg.split( "=" )[1] ) );
            } else if( arg.startsWith( "--customcss=" ) ) {
                generator.setCustomCssFile( new File( arg.split( "=" )[1] ) );
            } else if( arg.startsWith( "--format=" ) ) {
                String formatArg = arg.split( "=" )[1];
                Format format = Format.fromStringOrNull( formatArg );
                if( format == null ) {
                    System.err.println( "Illegal argument for --format: " + formatArg );
                    printUsageAndExit();
                }
                generator.setFormat( format );
            } else {
                printUsageAndExit();
            }
        }
    }

    public void setFormat( Format format ) {
        this.format = format;
    }

    public void generate() throws IOException {
        if( !getToDir().exists() && !getToDir().mkdirs() ) {
            log.error( "Could not create target directory " + getToDir() );
            return;
        }

        if( format == HTML ) {
            new StaticHtmlReportGenerator().generate( getToDir(), getSourceDir() );
            new SingleFileHtmlReportGenerator().generate( getToDir(), "allscenarios.html", getSourceDir() );
            if( getCustomCssFile() != null ) {
                if( !getCustomCssFile().canRead() ) {
                    log.info( "Cannot read customCssFile " + getCustomCssFile() + " skipping" );
                } else {
                    Files.copy( getCustomCssFile(), new File( getToDir(), "custom.css" ) );
                }
            }
        } else if( format == TEXT ) {
            new PlainTextReportGenerator().generate( getToDir(), getSourceDir() );
        }

    }

    private static void printUsageAndExit() {
        System.err.println( "Options: [--format=<format>] [--dir=<dir>] [--todir=<dir>] [--customcss=<cssfile>]" ); // NOSONAR
        System.err.println( "  <format> = html or text, default is html" );
        System.exit( 1 );
    }

    public File getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir( File sourceDir ) {
        this.sourceDir = sourceDir;
    }

    public File getToDir() {
        return toDir;
    }

    public void setToDir( File toDir ) {
        this.toDir = toDir;
    }

    public File getCustomCssFile() {
        return customCssFile;
    }

    public void setCustomCssFile( File customCssFile ) {
        this.customCssFile = customCssFile;
    }

}