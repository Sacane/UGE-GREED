# Strategy du design pattern du visitor


## Introduction

Le design pattern du visitor permet d'exécuter une action sur un ensemble d'objets sans avoir à connaître leur type. 
Cela permet de séparer le code de l'objet de celui de l'action à effectuer sur l'objet.

Cela permet également de faciliter l'ajout d'implémentation de nouveaux objets sans avoir à modifier le code de l'action à effectuer sur l'objet.

## Exemple

### Code

```java
public interface Visitable {
    void accept(Visitor visitor);
}

public interface Visitor {
    void visit(A a);
    void visit(B b);
    void visit(C c);
}


public class A implements Visitable {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

public class B implements Visitable {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

public class C implements Visitable {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

public class VisitorImpl implements Visitor {
    @Override
    public void visit(A a) {
        System.out.println("A");
    }

    @Override
    public void visit(B b) {
        System.out.println("B");
    }

    @Override
    public void visit(C c) {
        System.out.println("C");
    }
}

public class Main {
    public static void main(String[] args) {
        List<Visitable> visitables = new ArrayList<>();
        visitables.add(new A());
        visitables.add(new B());
        visitables.add(new C());

        Visitor visitor = new VisitorImpl();

        for (Visitable visitable : visitables) {
            visitable.accept(visitor);
        }
    }
}
//Output : A\nB\nC
```

## Dans le cadre du projet Greed

Dans notre cas, chaque Packet sera un visitable qui permettra de faire en sorte que le serveur
effectue une action selon le packet en cours de traitement.

````java
import java.net.ConnectException;

public interface Packet extends Visitable {
    void accept(Visitor visitor);
}

public interface Visitor {
    void visit(OKPacket packet);

    void visit(KOPacket packet);

    void visit(ConnectPacket packet);
}

public class OKPacket implements Packet {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

public class KOPacket implements Packet {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

public class ConnectPacket implements Packet {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

public class ServerVisitor implements Visitor {
    @Override
    public void visit(OKPacket packet) {
        System.out.println("Le serveur doit démarrer car il reçoit un OK Packet");
    }

    @Override
    public void visit(KOPacket packet) {
        System.out.println("Le serveur mother n'a pas accepter la connexion, on arrête");
    }

    @Override
    public void visit(ConnectPacket packet) {
        System.out.println("On reçoit une demande de connexion, il faut l'accepter selon l'état de notre serveur");
    }
}
````