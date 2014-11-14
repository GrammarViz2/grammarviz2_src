require(reshape)
require(lattice)
require(RColorBrewer)

data=read.table("data/GP_test.csv",as.is=T,header=F, sep=",")

p=wireframe(V3 ~ V2 * V1, data = data, scales = list(arrows = FALSE),
            drape = TRUE, colorkey = FALSE, pretty=FALSE, screen = list(z = 30, x = -70, y = 00),
            aspect = c(87/97, 0.6),
            xlim=range(data$V1), ylim=range(data$V2),
            main=paste("TEST"),
            col.regions = terrain.colors(1000, alpha = 1) )
p
