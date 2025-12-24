package per.midas.kurumishell

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class KurumiShellApplication

fun main(args: Array<String>) {
	runApplication<KurumiShellApplication>(*args)
}
