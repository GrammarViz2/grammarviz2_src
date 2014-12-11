require(ggplot2)
require(Cairo)
require(reshape)
require(scales)
require(RColorBrewer)
require(colorRamps)
require(grid)
library(gridExtra)
require(lattice)


dat = read.table("logs/integer.csv",header=F)
names(dat)=c("Error","Window","PAA","Alphabet")

dat[rev(order(dat[,1])),]
t=45
cols=c(green2red(t), rep("red",(100-t)))

par.set <-
  list(axis.line = list(col = "transparent"),
       clip = list(panel = "off"))
p0=cloud(PAA ~ Alphabet * Window, data = dat, col=cols[floor(dat$Error*100+1)],
         cex = .8,
         screen = list(z = 60, x = -70, y = 3),
         par.settings = par.set,panel.aspect = 1.2,
         scales = list(arrows = FALSE),
         drape = TRUE, colorkey = FALSE,pretty=TRUE,
         main="Integer sampler")
p0

#
dat2 = read.table("logs/continuous.csv",header=F)
names(dat2)=c("Error","Window","PAA","Alphabet")

dat2[rev(order(dat2[,1])),]
t=45
cols=c(green2red(t), rep("red",(100-t)))

par.set <-
  list(axis.line = list(col = "transparent"),
       clip = list(panel = "off"))
p1=cloud(PAA ~ Alphabet * Window, data = dat2, col=cols[floor(dat2$Error*100+1)],
         cex = .8,
         screen = list(z = 60, x = -70, y = 3),
         par.settings = par.set,panel.aspect = 1.2,
         scales = list(arrows = FALSE),
         drape = TRUE, colorkey = FALSE,pretty=TRUE,
         main="Continous sampler")
p1

print(arrangeGrob(p0, p1, ncol=2))
