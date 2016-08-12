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
write.table(y, "../data/aa_synth/sine_0_3a.txt", col.names = F, row.names = F)
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
write.table(y, "../data/aa_synth/sine_0_4a.txt", col.names = F, row.names = F)
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
write.table(y1, "../data/aa_synth/sine_0_5a.txt", col.names = F, row.names = F)
#
# sampling interval
#
dd = data.frame(x = 1:length(y1), y = y1)
p5 <- ggplot(dd[745:1070,], aes(x, y)) + geom_line() + ggtitle("Sine wave + anomaly")
p5
#
# **** SOME QC
#
dd = fread("../RCode/TKDD/sine_and_5anomalies.txt")
plot(dd$V1, type = "l")
#
# **** SOME QC
#
#
#
# Random walk
#
n <- length(dd$V1)
step <- c(0.05,-0.05)
x <- rep(0,n)
for (i in 2:n) {
  x[i] <- x[i - 1] + sample(step, 1)
}
plot(x, type = "l")
write.table(x, "../RCode/TKDD/random_walk.txt", col.names = F, row.names = F)
#
# combine the series and the random walk
#
dd$V2 <- dd$V1 + x
plot(dd$V2[745:1070], type = "l")
write.table(dd$V2, "../RCode/TKDD/sine_5anomalies_rwalk.txt", col.names = F, row.names = F)
#
# add a random noise to combined TS
#
dd$V2 <- dd$V1 + x + 0.2 * (rnorm(length(dd$V1)) - 0.5)
plot(dd$V2, type = "l")
write.table(dd$V2, "../RCode/TKDD/sine_5anomalies_rwalk_02noise.txt", col.names = F, row.names = F)
#
#
dd$V2 <- dd$V1 + x + 0.1 * (rnorm(length(dd$V1)) - 0.5)
plot(dd$V2, type = "l")
write.table(dd$V2, "../RCode/TKDD/sine_5anomalies_rwalk_01noise.txt", col.names = F, row.names = F)
#
#
dd$V2 <- dd$V1 + x + 0.3 * (rnorm(length(dd$V1)) - 0.5)
plot(dd$V2, type = "l")
write.table(dd$V2, "../RCode/TKDD/sine_5anomalies_rwalk_03noise.txt", col.names = F, row.names = F)
#
#
dd$V2 <- dd$V1 + x + 0.4 * (rnorm(length(dd$V1)) - 0.5)
plot(dd$V2, type = "l")
write.table(dd$V2, "../RCode/TKDD/sine_5anomalies_rwalk_04noise.txt", col.names = F, row.names = F)
#
#
dd$V2 <- dd$V1 + x + 0.5 * (rnorm(length(dd$V1)) - 0.5)
plot(dd$V2, type = "l")
write.table(dd$V2, "../RCode/TKDD/sine_5anomalies_rwalk_05noise.txt", col.names = F, row.names = F)
#
#

