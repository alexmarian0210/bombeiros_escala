
package com.example.bombeiros.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bombeiros.model.EscalaPlantao;
import com.example.bombeiros.model.Participacao;
import com.example.bombeiros.repository.BombeiroRepository;
import com.example.bombeiros.repository.EscalaPlantaoRepository;
import com.example.bombeiros.repository.ParticipacaoRepository;
import com.example.bombeiros.service.ReservaService;

import jakarta.servlet.http.HttpSession;

@Controller
public class EscalaController {

    private static final ZoneId FUSO =
            ZoneId.of("America/Sao_Paulo");

    @Autowired
    private EscalaPlantaoRepository escalaRepository;

    @Autowired
    private BombeiroRepository bombeiroRepository;

    @Autowired
    private ParticipacaoRepository participacaoRepository;

    @Autowired
    private ReservaService reservaService;

    // =========================================================
    // PAINEL INICIAL
    // =========================================================

    @GetMapping("/home")
    public String exibirInicio(Model model, HttpSession session) {

        if (!estaLogado(session)) {
            return "redirect:/login";
        }

        carregarDadosDoUsuario(model, session);

        Integer bombeiroId =
                (Integer) session.getAttribute("usuarioLogadoId");

        LocalDate hoje = LocalDate.now(FUSO);
        LocalDateTime agora = LocalDateTime.now(FUSO);

        String dia = hoje.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

        model.addAttribute("dataAtual", hoje);
        model.addAttribute(
                "diaDaSemana",
                dia.substring(0, 1).toUpperCase() + dia.substring(1)
        );

        List<Participacao> reservasAtivas =
                participacaoRepository
                        .findReservasAtivasDoBombeiro(bombeiroId);

        List<EscalaPlantao> todosPlantoes =
                escalaRepository.findAll();

        // Plantões de hoje: MANHÃ, TARDE e NOITE
        List<EscalaPlantao> plantoesHoje =
                todosPlantoes.stream()
                        .filter(e -> e.getData() != null)
                        .filter(e -> e.getData().equals(hoje))
                        .sorted(Comparator.comparingInt(
                                e -> ordemTurno(e.getTurno())
                        ))
                        .toList();

        // Próximos três plantões, considerando data e horário
        List<EscalaPlantao> proximosPlantoes =
                todosPlantoes.stream()
                        .filter(e -> e.getData() != null)
                        .filter(e -> !reservaService
                                .obterDataHoraInicioPlantao(e)
                                .isBefore(agora))
                        .sorted(
                                Comparator.comparing(
                                        reservaService::obterDataHoraInicioPlantao
                                )
                        )
                        .limit(3)
                        .toList();

        // Próxima reserva do usuário
        Participacao proximaReserva =
                reservasAtivas.stream()
                        .filter(p -> p.getEscalaPlantao() != null)
                        .filter(p -> !reservaService
                                .obterDataHoraInicioPlantao(
                                        p.getEscalaPlantao())
                                .isBefore(agora))
                        .min(Comparator.comparing(
                                p -> reservaService
                                        .obterDataHoraInicioPlantao(
                                                p.getEscalaPlantao())
                        ))
                        .orElse(null);

        // Quantidade de plantões futuros que ainda possuem vagas
        long totalDisponiveis =
                todosPlantoes.stream()
                        .filter(e -> e.getData() != null)
                        .filter(e -> !reservaService
                                .obterDataHoraInicioPlantao(e)
                                .isBefore(agora))
                        .filter(e -> reservaService
                                .calcularVagasDisponiveis(e) > 0)
                        .count();

        model.addAttribute(
                "qtdMinhasReservas",
                reservasAtivas.size()
        );

        model.addAttribute(
                "qtdPlantoesDisponiveis",
                totalDisponiveis
        );

        model.addAttribute("qtdNotificacoes", 0);

        model.addAttribute("proximaReserva", proximaReserva);
        model.addAttribute("plantoesHoje", plantoesHoje);
        model.addAttribute("proximosPlantoes", proximosPlantoes);
        model.addAttribute("reservaService", reservaService);

        return "TelaInicial";
    }

    // =========================================================
    // TELA DE ESCALAS
    // =========================================================

    @GetMapping("/escalas")
    public String exibirEscalas(Model model, HttpSession session) {

        if (!estaLogado(session)) {
            return "redirect:/login";
        }

        carregarDadosDoUsuario(model, session);

        LocalDate hoje = LocalDate.now(FUSO);

        // Último dia do mês atual
        LocalDate ultimoDiaMes =
                hoje.withDayOfMonth(hoje.lengthOfMonth());

        String[] turnos = {
                "MANHA",
                "TARDE",
                "NOITE"
        };

        // Busca os plantões que já existem no banco
        List<EscalaPlantao> existentes =
                escalaRepository.findAll();

        // Monta uma chave para identificar data + turno
        Set<String> chavesExistentes =
                existentes.stream()
                        .filter(e -> e.getData() != null)
                        .filter(e -> e.getTurno() != null)
                        .map(e ->
                                e.getData() + "-" +
                                normalizarTurno(e.getTurno())
                        )
                        .collect(Collectors.toSet());

        // Lista de plantões que precisam ser criados
        List<EscalaPlantao> novosPlantoes =
                new java.util.ArrayList<>();

        // Cria os três turnos para cada dia restante do mês
        for (LocalDate data = hoje;
             !data.isAfter(ultimoDiaMes);
             data = data.plusDays(1)) {

            for (String turno : turnos) {

                String chave = data + "-" + turno;

                if (!chavesExistentes.contains(chave)) {

                    EscalaPlantao novo = new EscalaPlantao();

                    novo.setData(data);
                    novo.setTurno(turno);

                    novosPlantoes.add(novo);
                }
            }
        }

        // Salva os plantões que ainda não existiam
        if (!novosPlantoes.isEmpty()) {
            escalaRepository.saveAll(novosPlantoes);
        }

        // Busca novamente os plantões após a criação
        List<EscalaPlantao> escalas =
                escalaRepository.findAll().stream()

                        // Remove registros sem data
                        .filter(e -> e.getData() != null)

                        // Não mostra histórico anterior a hoje
                        .filter(e -> !e.getData().isBefore(hoje))

                        // Mostra somente até o último dia deste mês
                        .filter(e -> !e.getData().isAfter(ultimoDiaMes))

                        // Ordena por data e turno:
                        // MANHÃ -> TARDE -> NOITE
                        .sorted(
                                Comparator
                                        .comparing(EscalaPlantao::getData)
                                        .thenComparingInt(
                                                e -> ordemTurno(e.getTurno())
                                        )
                        )
                        .toList();

        Integer bombeiroId =
                (Integer) session.getAttribute("usuarioLogadoId");

        List<Participacao> minhasReservas =
                participacaoRepository
                        .findReservasAtivasDoBombeiro(bombeiroId);

        model.addAttribute("escalas", escalas);
        model.addAttribute("minhasReservas", minhasReservas);
        model.addAttribute("dataHoje", hoje);
        model.addAttribute("reservaService", reservaService);

        return "Escalas";
    }

    // =========================================================
    // PLANTÕES DISPONÍVEIS
    // =========================================================

    @GetMapping("/plantoes-disponiveis")
    public String listarPlantoesDisponiveis(
            HttpSession session) {

        if (!estaLogado(session)) {
            return "redirect:/login";
        }

        return "redirect:/escalas";
    }

    // =========================================================
    // FORMULÁRIO DE RESERVA
    // =========================================================

    @GetMapping("/reservar/formulario")
    public String formularioReserva(
            @RequestParam("escalaId") Integer escalaId,
            Model model,
            HttpSession session) {

        if (!estaLogado(session)) {
            return "redirect:/login";
        }

        carregarDadosDoUsuario(model, session);

        EscalaPlantao escala =
                escalaRepository.findById(escalaId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Plantão não encontrado."
                                )
                        );

        LocalDateTime inicioPlantao =
                reservaService.obterDataHoraInicioPlantao(escala);

        if (!LocalDateTime.now(FUSO).isBefore(inicioPlantao)) {
            return "redirect:/escalas";
        }

        model.addAttribute("escala", escala);

        model.addAttribute(
                "bombeiroNome",
                session.getAttribute("usuarioLogadoNome")
        );

        return "FormularioReserva";
    }

    // =========================================================
    // CONFIRMAR RESERVA
    // =========================================================

    @PostMapping("/reservar/confirmar")
    public String confirmarReserva(
            @RequestParam("escalaId") Integer escalaId,
            @RequestParam(
                    value = "observacoes",
                    required = false
            ) String observacoes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!estaLogado(session)) {
            return "redirect:/login";
        }

        Integer bombeiroId =
                (Integer) session.getAttribute("usuarioLogadoId");

        try {

            reservaService.realizarReserva(
                    escalaId,
                    bombeiroId,
                    observacoes
            );

            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Reserva efetuada com sucesso!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "mensagemErro",
                    e.getMessage()
            );
        }

        return "redirect:/escalas";
    }

    // =========================================================
    // MINHAS RESERVAS
    // =========================================================

    @GetMapping("/minhas-reservas")
    public String minhasReservas(
            Model model,
            HttpSession session) {

        if (!estaLogado(session)) {
            return "redirect:/login";
        }

        carregarDadosDoUsuario(model, session);

        Integer bombeiroId =
                (Integer) session.getAttribute("usuarioLogadoId");

        List<Participacao> reservas =
                participacaoRepository
                        .findReservasAtivasDoBombeiro(bombeiroId);

        model.addAttribute("reservas", reservas);
        model.addAttribute("reservaService", reservaService);

        return "MinhasReservas";
    }

    // =========================================================
    // CANCELAR RESERVA
    // =========================================================

    @PostMapping("/reservar/cancelar")
    public String cancelarMinhaReserva(
            @RequestParam("participacaoId") Integer participacaoId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!estaLogado(session)) {
            return "redirect:/login";
        }

        Integer bombeiroId =
                (Integer) session.getAttribute("usuarioLogadoId");

        try {

            reservaService.cancelarReserva(
                    participacaoId,
                    bombeiroId
            );

            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Sua reserva foi cancelada com sucesso."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "mensagemErro",
                    e.getMessage()
            );
        }

        return "redirect:/escalas";
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private boolean estaLogado(HttpSession session) {

        return session.getAttribute("usuarioLogadoId") != null;
    }

    private void carregarDadosDoUsuario(
            Model model,
            HttpSession session) {

        String perfil =
                (String) session.getAttribute("usuarioLogadoPerfil");

        model.addAttribute(
                "usuarioNome",
                session.getAttribute("usuarioLogadoNome")
        );

        model.addAttribute(
                "perfilNome",
                nomeDoPerfil(perfil)
        );

        model.addAttribute(
                "ehAdmin",
                "ADMINISTRADOR".equals(perfil)
        );
    }

    private String nomeDoPerfil(String perfil) {

        if (perfil == null) {
            return "Bombeiro";
        }

        return switch (perfil) {

            case "ADMINISTRADOR" -> "Administrador";

            case "CHEFE_EQUIPE" -> "Chefe de equipe";

            default -> "Bombeiro";
        };
    }

    // Define a ordem correta dos turnos
    private int ordemTurno(String turno) {

        if (turno == null) {
            return 4;
        }

        return switch (normalizarTurno(turno)) {

            case "MANHA" -> 1;

            case "TARDE" -> 2;

            case "NOITE" -> 3;

            default -> 4;
        };
    }

    private String normalizarTurno(String turno) {

        if (turno == null) {
            return "";
        }

        return turno.trim()
                .toUpperCase(Locale.ROOT)
                .replace("Ã", "A");
    }
}
