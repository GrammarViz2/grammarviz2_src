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
dat <- fread("../data/aa_synth/sine_0_a2.txt")

ts <- data.frame(x = 1:(length(dat$V1)), y = dat$V1)
#
p1 <- ggplot(ts, aes(x, y)) + geom_line(color = "blue", lwd = 0.3) + theme_bw() +
  ggtitle("Synthetic dataset")
p1 
#
stats <- fread("../RCode/TKDD/sine_5anomalies_rwalk_sampler_out.txt")
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
