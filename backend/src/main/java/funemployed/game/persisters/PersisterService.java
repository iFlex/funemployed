package funemployed.game.persisters;

import funemployed.game.GameInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;

@Service
public class PersisterService {
    @Autowired
    Persister persister;

    Executor executor= new ConcurrentTaskExecutor();

    public void setPersister(Persister p){
        this.persister = p;
    }

    public void update(GameInstance gameInstance){
        executor.execute(() -> {
            persister.save(gameInstance);
        });
    }

    public void delete(String gid){
        executor.execute(() -> {
            persister.delete(gid);
        });
    }

    public List<GameInstance> load(){
        return persister.loadAll();
    }
}
