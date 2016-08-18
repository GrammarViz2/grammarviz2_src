require(reshape)
require(scales)
require(data.table)
#
require(Cairo)
require(ggplot2)
require(RColorBrewer)
require(grid)
require(gridExtra)
require(lattice)
#
# #1
#
sine <- fread("../RCode/TKDD/sine_wave.txt")
sine <- data.frame(x = 1:(length(sine$V1)), y = sine$V1)
#
p1 <- ggplot(sine, aes(x, y)) + geom_line(lwd = 0.3, color = "blue") + theme_bw() +
  scale_y_continuous(breaks = seq(-1, 1, by = 1)) +
  theme(axis.title.x = element_blank(),axis.title.y = element_blank(),
        panel.grid.major = element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Sine wave")
p1 
#
# #2
#
sine_anomaly <- fread("../RCode/TKDD/sine_and_5anomalies.txt")
sine_anomaly <- data.frame(x = 1:(length(sine_anomaly$V1)), y = sine_anomaly$V1)
#
p2 <- ggplot(sine_anomaly, aes(x, y)) + geom_line( lwd = 0.3, color="blue") + theme_bw()  +
  scale_y_continuous(breaks = seq(-1, 1, by = 1)) +
  theme(axis.title.x = element_blank(),axis.title.y = element_blank(),
        panel.grid.major = element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Sine wave and 5 anomalies")
p2 
#
df_red <- sine_anomaly; df_red$y <- rep(NA,1242);
df_red[205:217,] <- sine_anomaly[205:217,]
df_red[360:392,] <- sine_anomaly[360:392,]
df_red[726:740,] <- sine_anomaly[726:740,]
df_red[500:545,] <- sine_anomaly[500:545,]
df_red[1081:1095,] <- sine_anomaly[1081:1095,]
p2 <- p2 + geom_line(aes(df_red$x, df_red$y), color = "brown3", size = 1)
p2
#
# #3
#
random_walk <- fread("../RCode/TKDD/random_walk.txt")
random_walk <- data.frame(x = 1:(length(random_walk$V1)), y = random_walk$V1)
#
p3 <- ggplot(random_walk, aes(x, y)) + geom_line( lwd = 0.3, color="blue") + theme_bw() +
  scale_y_continuous(breaks = seq(0, 2, by = 1)) +
  theme(axis.title.x = element_blank(),axis.title.y = element_blank(),
        panel.grid.major = element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Random walk trend")
p3 
#
# #4
#
trending_anomaly <- fread("../RCode/TKDD/sine_5anomalies_rwalk.txt")
trending_anomaly <- data.frame(x = 1:(length(trending_anomaly$V1)), y = trending_anomaly$V1)
#
p4 <- ggplot(trending_anomaly, aes(x, y)) + geom_line( lwd = 0.3, color="blue") + theme_bw() +
  theme(axis.title.x = element_blank(),axis.title.y = element_blank(),
        panel.grid.major = element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Sine wave + anomalies + random walk trend")
p4 
#
df_red1 <- trending_anomaly; df_red1$y <- rep(NA,1242);
df_red1[205:217,] <- trending_anomaly[205:217,]
df_red1[360:392,] <- trending_anomaly[360:392,]
df_red1[726:740,] <- trending_anomaly[726:740,]
df_red1[500:545,] <- trending_anomaly[500:545,]
df_red1[1081:1095,] <- trending_anomaly[1081:1095,]
p4 <- p4 + geom_line(aes(df_red1$x, df_red1$y), color = "brown3", size = 1)
p4
#
#
# #5
#
trending_anomaly1 <- fread("../RCode/TKDD/sine_5anomalies_rwalk_01noise.txt")
trending_anomaly1 <- data.frame(x = 1:(length(trending_anomaly1$V1)), y = trending_anomaly1$V1)
#
p5 <- ggplot(trending_anomaly1, aes(x, y)) + geom_line( lwd = 0.3, color="blue")+ theme_bw() +
  theme(axis.title.x = element_blank(),axis.title.y = element_blank(),
        panel.grid.major = element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Sine wave + anomalies + random walk trend + noise 10%")
p5 
#
df_red2 <- trending_anomaly1; df_red2$y <- rep(NA,1242);
df_red2[205:217,] <- trending_anomaly1[205:217,]
df_red2[360:392,] <- trending_anomaly1[360:392,]
df_red2[726:740,] <- trending_anomaly1[726:740,]
df_red2[500:545,] <- trending_anomaly1[500:545,]
df_red2[1081:1095,] <- trending_anomaly1[1081:1095,]
p5 <- p5 + geom_line(aes(df_red2$x, df_red2$y), color = "brown3", size = 1)
p5
#
# #6
#
trending_anomaly2 <- fread("../RCode/TKDD/sine_5anomalies_rwalk_02noise.txt")
trending_anomaly2 <- data.frame(x = 1:(length(trending_anomaly2$V1)), y = trending_anomaly2$V1)
#
p6 <- ggplot(trending_anomaly2, aes(x, y)) + geom_line( lwd = 0.3, color="blue") + theme_bw() +
  theme(axis.title.x = element_blank(),axis.title.y = element_blank(),
        panel.grid.major = element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Sine wave + anomalies + random walk trend + noise 20%")
p6 
#
df_red3 <- trending_anomaly2; df_red3$y <- rep(NA,1242);
df_red3[205:217,] <- trending_anomaly2[205:217,]
df_red3[360:392,] <- trending_anomaly2[360:392,]
df_red3[726:740,] <- trending_anomaly2[726:740,]
df_red3[500:545,] <- trending_anomaly2[500:545,]
df_red3[1081:1095,] <- trending_anomaly2[1081:1095,]
p6 <- p6 + geom_line(aes(df_red3$x, df_red3$y), color = "brown3", size = 1)
p6
#
#
# #7
#
trending_anomaly3 <- fread("../RCode/TKDD/sine_5anomalies_rwalk_03noise.txt")
trending_anomaly3 <- data.frame(x = 1:(length(trending_anomaly3$V1)), y = trending_anomaly3$V1)
#
p7 <- ggplot(trending_anomaly3, aes(x, y)) + geom_line( lwd = 0.3, color="blue") + theme_bw() +
  theme(axis.title.x = element_blank(),axis.title.y = element_blank(),
        panel.grid.major = element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Sine wave + anomalies + random walk trend + noise 30%")
p7 
#
df_red4 <- trending_anomaly3; df_red4$y <- rep(NA,1242);
df_red4[205:217,] <- trending_anomaly3[205:217,]
df_red4[360:392,] <- trending_anomaly3[360:392,]
df_red4[726:740,] <- trending_anomaly3[726:740,]
df_red4[500:545,] <- trending_anomaly3[500:545,]
df_red4[1081:1095,] <- trending_anomaly3[1081:1095,]
p7 <- p7 + geom_line(aes(df_red4$x, df_red4$y), color = "brown3", size = 1)
p7
#
#
# #8
#
trending_anomaly4 <- fread("../RCode/TKDD/sine_5anomalies_rwalk_04noise.txt")
trending_anomaly4 <- data.frame(x = 1:(length(trending_anomaly4$V1)), y = trending_anomaly4$V1)
#
p8 <- ggplot(trending_anomaly4, aes(x, y)) + geom_line( lwd = 0.3, color="blue") + theme_bw() +
  theme(axis.title.x = element_blank(),axis.title.y = element_blank(),
        panel.grid.major = element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Sine wave + anomalies + random walk trend + noise 40%")
p8 
#
df_red5 <- trending_anomaly4; df_red5$y <- rep(NA,1242);
df_red5[205:217,] <- trending_anomaly4[205:217,]
df_red5[360:392,] <- trending_anomaly4[360:392,]
df_red5[726:740,] <- trending_anomaly4[726:740,]
df_red5[500:545,] <- trending_anomaly4[500:545,]
df_red5[1081:1095,] <- trending_anomaly4[1081:1095,]
p8 <- p8 + geom_line(aes(df_red5$x, df_red5$y), color = "brown3", size = 1)
p8
#
grid.arrange(p1, p2, p3, p4, p5, p6, p7, p8, layout_matrix = cbind(c(1, 3, 5, 7), c(2, 4, 6, 8)))
#
Cairo(width = 1050, height = 600, 
      file="synth_00.pdf", 
      type="pdf", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(grid.arrange(p1, p2, p3, p4, p5, p6, p7, p8, layout_matrix = cbind(c(1, 3, 5, 7), c(2, 4, 6, 8))))
dev.off()



stats <- fread("../RCode/TKDD/sine_5anomalies_rwalk_04noise_sampler_out.txt")
names(stats) <- c("int_start", "int_end", "window", "paa", "alphabet", "approxDist", "grammarSize",
                  "grammarRules", "compressedGrammarSize", "prunedRules", "isCovered", "coverage")
#
p2 <- ggplot(data = stats, aes(window)) + geom_density(fill = "cornflowerblue") + 
  theme_bw() + ggtitle("Sampled window size")
p2
#
p3 <- ggplot(data = stats, aes(paa)) + geom_density(fill = "cornflowerblue") + 
  theme_bw() + ggtitle("Sampled PAA size")
p3
#
p4 <- ggplot(data = stats, aes(alphabet)) + geom_density(fill = "cornflowerblue") + 
  theme_bw() + ggtitle("Sampled Alphabet size")
p4

grid.arrange(p2,p3,p4, layout_matrix = cbind(c(1), c(2), c(3)))


Cairo(width = 900, height = 300, 
      file="synth2.pdf", 
      type="pdf", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(grid.arrange(p2,p3,p4, layout_matrix = cbind(c(1), c(2), c(3))))
dev.off()

mean(stats$window)
mean(stats$paa)
mean(stats$alphabet)
