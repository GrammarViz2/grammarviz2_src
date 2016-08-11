library(ggplot2)
#
# smooth, normal sine
#
t <- seq(0, pi*120, 0.3)
y <- sin(t)
p <- ggplot(data.frame(x = t, y = y), aes(x, y)) + geom_line() + ggtitle("Sine wave")
p
write.table(y, "../data/aa_synth/sine_0.txt", col.names = F, row.names = F)
#
# adding random noise
#
yn <- sin(t) + 0.1 * (rnorm(length(t)) - 0.5)
p1 <- ggplot(data.frame(x = t, y = yn), aes(x, y)) + geom_line() + ggtitle("Sine wave + random noise")
p1
write.table(yn, "../data/aa_synth/sine_noise.txt", col.names = F, row.names = F)
#
# adding three anomalies of length pi
#
anomaly_pos = c(runif(1) * (max(t) - pi *2), runif(1) * (max(t) - pi *2), 
                runif(1) * (max(t) - pi *2))
points <- which(t >= anomaly_pos[1] & t <= anomaly_pos[1] + pi)
y[points] <- 1
points <- which(t >= anomaly_pos[2] & t <= anomaly_pos[2] + pi)
y[points] <- 0
points <- which(t >= anomaly_pos[3] & t <= anomaly_pos[3] + pi)
y[points] <- -1
p2 <- ggplot(data.frame(x = 1:length(y), y = y), aes(x, y)) + geom_line() + ggtitle("Sine wave + anomaly")
p2
write.table(y, "../data/aa_synth/sine_1_a.txt", col.names = F, row.names = F)
#
# adding an anomaly of stretch and compression
#
# compression
#
k = 376.8/(length(t))
#anomaly_pos2 = c( runif(1) * (max(t) - pi *6), runif(1) * (max(t) - pi *6) )
anomaly_pos2 <- c( 360*k, 508*k)
points2 <- which(t >= anomaly_pos2[1] & t <= anomaly_pos2[1] + pi * 6)
points2 <- points2[ seq(1, length(points2), by = 2) ]
y <- y[ -points2 ]
p3 <- ggplot(data.frame(x = 1:length(y), y = y), aes(x, y)) + geom_line() + ggtitle("Sine wave + anomaly")
p3
write.table(y, "../data/aa_synth/sine_1_a1.txt", col.names = F, row.names = F)
#
# stretch
# 
points3 <- which(t >= anomaly_pos2[2] & t <= anomaly_pos2[2] + pi * 1.5)
points3_new <- sin( seq(t[min(points3)], t[max(points3)], by = 0.15) - length(points2) * 0.3)
y1 <- y
y[points3]
y1 <- c( y[1:(min(points3))], points3_new, y[(max(points3)):length(y)])
p4 <- ggplot(data.frame(x = 1:length(y1), y = y1), aes(x, y)) + geom_line() + ggtitle("Sine wave + anomaly")
p4
write.table(y1, "../data/aa_synth/sine_0_a2.txt", col.names = F, row.names = F)
#
# sampling interval
#
dd = data.frame(x = 1:length(y1), y = y1)
p5 <- ggplot(dd[745:1070,], aes(x, y)) + geom_line() + ggtitle("Sine wave + anomaly")
p5


p3 <- ggplot(data.frame(x = t, y = y), aes(x, y)) + geom_line() + ggtitle("Sine wave + anomaly")
p3
df_red <- data.frame(x = t, y = y);
df_red$y <- rep(NA,length(y)); df_red$x <- rep(NA,length(t));
df_red$y[185:290] <- y[185:290]; df_red$x[185:290] <- t[185:290]
p4 <- p3 + geom_line(aes(y=df_red$y,x=df_red$x), color = "brown3", size = 1)
p4

write.table(y, "../data/aa_synth/sine_0_a2.txt", col.names = F, row.names = F)

#
#
#
library(data.table)
dd = fread("../res.txt")
density(dd$V3)
ggplot(data=dd, aes(dd$V5)) + geom_density()
plot(y[18:100])
