library(ggplot2)
library(reshape)
library(plyr)
library(dplyr)
#
series=read.table("../data/ecg0606_1.csv")
sample=read.table("paperworks/runlogs/ecg0606.log.csv",sep=",",header=T)
#
p_series=ggplot(
  data.frame(x=seq(1:length(series$V1)),y=series$V1),
  aes(x=x,y=y)) + geom_line() + theme_bw()
p_series
#
best_discord=data.frame( xstart = sample$discord_start,
                         xend = sample$discord_end,
                         y = seq(1:length(sample$discord_end))
                       )