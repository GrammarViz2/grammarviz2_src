require(reshape)
require(lattice)
require(RColorBrewer)
require(akima)
require(rgl)
require(ggplot2)

data=read.table("data/test.csv",as.is=T,header=F, sep=",")
p <- ggplot(data, aes(x=V2,y=V3))
p + geom_tile(aes(fill=V3),size=50)


p=wireframe(V3 ~ V2 * V1, data = data, scales = list(arrows = FALSE),
            drape = TRUE, colorkey = FALSE, pretty=FALSE, screen = list(z = 30, x = -70, y = 00),
            aspect = c(87/97, 0.6),
            xlim=range(data$V1), ylim=range(data$V2),
            main=paste("TEST"),
            col.regions = terrain.colors(1000, alpha = 1) )
p


data_e=read.table("data/GP_test.csv",as.is=T,header=F, sep=",")

p=wireframe(V3 ~ V2 * V1, data = data_e, scales = list(arrows = FALSE),
            drape = TRUE, colorkey = FALSE, pretty=FALSE, screen = list(z = 30, x = -70, y = 00),
            aspect = c(87/97, 0.6),
            xlim=range(data_e$V2), ylim=range(data_e$V1),
            main=paste("TEST"),
            col.regions = terrain.colors(1000, alpha = 1) )
p

slice=dat[dat$grammarSize>900 & dat$grammarSize<1100,]
plot3d(slice)
slice[with(slice, order(totalZeroes)), ]
dat[dat$totalZeroes==1,]

xyz <- with(data, interp(x=V1,y=V2,z=V3, 
                          xo=sort(unique(V1)), duplicate="mean", extrap=FALSE ))
with(xyz, persp3d(x,y,z, col=heat.colors(length(z))[rank(z)], xlab='grammarSize', 
                  ylab='approximationDistance', zlab='maxZeroRunLength', main=''))


p <- ggplot(data, aes(x=V1,y=V2))
p + geom_tile(aes(fill=V3),size=50)

p <- ggplot(data_e, aes(x=V1,y=V2))
p + geom_point(aes(color=V3),size=10,alpha=0.8)
