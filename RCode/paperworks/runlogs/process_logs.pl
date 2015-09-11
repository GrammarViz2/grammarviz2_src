use strict;
use warnings;

use Getopt::Long;
use File::Basename;
use IO::File;

my $folder = "sampler_logs";

GetOptions(
	"folder=s" => \$folder    # a folder with logs
) or die("Error: folder name is not supplied.\n");

my @files = glob "$folder/*.log";
foreach my $file (@files) {

	print "processing " . $file . "\n";

	my $log_fh   = IO::File->new( $file, 'r' );
	my $filename = basename($file) . ".csv";
	my $dump_fh  = IO::File->new( $filename, 'w' );

#
# consoleLogger.info("# " + WINDOW_SIZE + "," + PAA_SIZE + "," + ALPHABET_SIZE + ","
# +p.getApproxDist() +"," +p.getGrammarSize()+"," +p.getCompressedGrammarSize()+
# "," +p.getCoverage() +"," + discords.get(0).getPosition() + ","
# + String.valueOf(discords.get(0).getPosition() + discords.get(0).getLength()));
#
	$dump_fh->print(
		    "win_size,paa_size,a_size,approx,gr_size,"
		  . "gr_size_compressed,coverage,discord_start,discord_end,max_freq\n"
	);

	# Read in line at a time
	while ( my $line = $log_fh->getline() ) {
		if ( $line =~ "# " ) {
			chomp($line);
			my ($data_line) = ( $line =~ /^.*# (.*$)/s );

			# print $data_line . "\n";
			$dump_fh->print( $data_line . "\n" );
		}
	}
	$dump_fh->close();
}
