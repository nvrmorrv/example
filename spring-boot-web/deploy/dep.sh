docker login
docker build -t nvrmorrv/enode:$(date +%F) ../encryption-node
docker push nvrmorrv/enode:$(date +%F)
docker build -t nvrmorrv/mnode:$(date +%F) ../master-node
docker push nvrmorrv/mnode:$(date +%F)

