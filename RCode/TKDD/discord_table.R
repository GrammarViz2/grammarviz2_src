library(data.table)
library(plyr)
#
dat_sine <- fread("TKDD/sine_and_5anomalies_discord_res.txt")
dat_sine <- fread("TKDD/sine_5anomalies_rwalk_discord_res.txt")
dat_sine <- fread("TKDD/sine_5anomalies_rwalk_01noise_discord_res_10.txt")
dat_sine <- fread("TKDD/sine_5anomalies_rwalk_02noise_discord_res_10.txt")
dat_sine <- fread("TKDD/sine_5anomalies_rwalk_03noise_discord_res.txt")
dat_sine <- fread("TKDD/sine_5anomalies_rwalk_04noise_discord_res.txt")

names(dat_sine) <- c("window", "paa", "alphabet", paste("d", c(1:5), sep = ""))

dat_sine$rank <- 0
for (i in 1:(length(dat_sine$rank))) {
  rank <- 0
  row <- unlist(dat_sine[i, ])
  for (j in 4:9) {
    if ( row[j] > 0 ) {
      rank <- rank + 1
    }
  }
  row <- dat_sine[i, ]$rank <- rank
}
arrange(dat_sine, rank)


mean(dat_sine$rank)
sd(dat_sine$rank)


table(dat_sine$d1 > 0)
table(dat_sine$d2 > 0)
table(dat_sine$d3 > 0)
table(dat_sine$d4 > 0)
table(dat_sine$d5 > 0)

#
stats <- fread("TKDD/sine_and_5anomalies_out.txt")
stats <- fread("TKDD/sine_5anomalies_rwalk_sampler_out.txt")
stats <- fread("TKDD/sine_5anomalies_rwalk_01noise_sampler_out.txt")
stats <- fread("TKDD/sine_5anomalies_rwalk_02noise_sampler_out.txt")
stats <- fread("TKDD/sine_5anomalies_rwalk_03noise_sampler_out.txt")
stats <- fread("TKDD/sine_5anomalies_rwalk_04noise_sampler_out.txt")

names(stats) <- c("int_start", "int_end", "window", "paa", "alphabet", "approxDist", "grammarSize",
                  "grammarRules", "compressedGrammarSize", "prunedRules", "isCovered", "coverage")

mean(stats$window)
mean(stats$paa)
mean(stats$alphabet)
