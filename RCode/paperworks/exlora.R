library(ggplot2)
library(reshape)
library(plyr)
library(dplyr)
library(grid)
library(gridExtra)
#
series=read.table("../data/ann_gun_CentroidA1.csv")
sample=read.table("paperworks/runlogs/video.log.csv",sep=",",header=T)
sample$ratio = sample$gr_size_compressed / sample$gr_size
sample = arrange(sample,ratio)
#
p_series = ggplot(
  data.frame(x=seq(1:length(series$V1)),y=series$V1),
  aes(x=x,y=y)) + geom_line() + theme_bw()
p_series
#
best_discord=data.frame( xstart = sample$discord_start,
                         xend = sample$discord_end,
                         y = seq(1:length(sample$discord_end))
                       )
#
p_discord = ggplot(data=best_discord,aes(x=xstart,y=y)) + geom_segment(aes(xend=xend,yend=y)) + theme_bw()
p_discord

grid.arrange(p_series, p_discord)

plot(sample$approx,(sample$gr_size-sample$gr_size_compressed))
lmp <- lm(sample$approx ~ (sample$gr_size-sample$gr_size_compressed))
plot(lmp)

plot(sample$gr_size_compressed, sample$gr_size)
dd=data.frame(sample$approx, (sample$gr_size_compressed-sample$gr_size))
pairs(dd)

