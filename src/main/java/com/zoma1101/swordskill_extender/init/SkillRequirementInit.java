package com.zoma1101.swordskill_extender.init;

import com.zoma1101.swordskill.swordskills.SkillData;
import com.zoma1101.swordskill.swordskills.SwordSkillRegistry;

import java.util.HashMap;
import java.util.Map;

public class SkillRequirementInit {

    // 内部名称を "Sword" と "DualSword" に統一
    private static final String[] ALL_WEAPON_TYPES = {
            "Greatsword", "Spear", "Mace", "Rapier", "Dagger",
            "Katana", "Claw", "Scythe", "Battleaxe", "DualSword", "Sword"
    };

    /**
     * 全スキルの熟練度要件を一括設定します。
     * FMLCommonSetupEvent の enqueueWork 内で呼び出してください。
     */
    public static void register() {
        setSkillRequirement("slant", 30);
        setSkillRequirement("horizontal", 80);
        setSkillRequirement("horizontal_arc", 200);
        setSkillRequirement("horizontal_square", 500);
        setSkillRequirement("vertical", 80);
        setSkillRequirement("vertical_arc", 200);
        setSkillRequirement("vertical_square", 500);
        setSkillRequirement("x_break", 100);
        setSkillRequirement("sharp_neil", 300);
        setSkillRequirement("holy_cross_brade", 300);
        setSkillRequirement("rage_spike", 100);
        setSkillRequirement("sonic_reap", 200);
        setSkillRequirement("savage_fulcrum", 350);
        setSkillRequirement("howling_octave", 500);
        setSkillRequirement("vorpal_strike", 600);
        setSkillRequirement("star_sword", 550);
        //setSkillRequirement("world_nova", 100);

        setSkillRequirement("wall_wind", 100);
        setSkillRequirement("excite", 50);
        setSkillRequirement("excite_line", 500);
        setSkillRequirement("excite_triangle", 700);
        setSkillRequirement("king_axe", 1000);
        setSkillRequirement("lumber_jack", 200);
        setSkillRequirement("grand_upper", 350);
        setSkillRequirement("blood_finisher", 300);

        setSkillRequirement("thrusts", 80);
        setSkillRequirement("spinning_spear", 200);
        setSkillRequirement("spark_thrust", 300);
        setSkillRequirement("sorvelte_charge", 400);
        setSkillRequirement("longlonglong", 1000);

        setSkillRequirement("end_revolver", 100);
        setSkillRequirement("ascade_refraction", 300);
        setSkillRequirement("double_circular", 500);

        setSkillRequirement("reaper", 80);
        setSkillRequirement("sakura_mai", 200);
        setSkillRequirement("zekku", 300);
        setSkillRequirement("tumuzi_guruma", 300);
        setSkillRequirement("gengetu", 700);

        setSkillRequirement("cascade", 80);
        setSkillRequirement("cyclone", 200);
        setSkillRequirement("stan_hazard", 300);
        setSkillRequirement("slash_blade", 400);

        setSkillRequirement("linear", 50);
        setSkillRequirement("parallel_sting", 200);
        setSkillRequirement("quadraple_pain", 400);
        setSkillRequirement("cruci_fiction", 500);
        setSkillRequirement("star_splash", 300);
        setSkillRequirement("stary_tier", 500);
        setSkillRequirement("shooting_star", 700);
        setSkillRequirement("flashing_penetrator", 1000);

        setSkillRequirement("canine", 10);
        setSkillRequirement("face_liner", 200);
        setSkillRequirement("fad_edge", 300);
        setSkillRequirement("rapid_bite", 400);
        setSkillRequirement("octagon_edge", 500);
        setSkillRequirement("kodati", 600);

        setSkillRequirement("barrick", 100);
        setSkillRequirement("reaper_of_life", 300);
        setSkillRequirement("reaper_of_death", 300);
        setSkillRequirement("shadow_scythe", 500);
        setSkillRequirement("requiem", 600);
        setSkillRequirement("storm", 200);
        setSkillRequirement("storm_mirror", 400);

        setSkillRequirement("talon", 50);
        setSkillRequirement("hant", 250);
        setSkillRequirement("hant_killer", 350);
        setSkillRequirement("tiger_claw", 100);
        setSkillRequirement("dragon_claw", 500);
        setSkillRequirement("beast_claw", 350);
        setSkillRequirement("golden_claw", 200);
        setSkillRequirement("fast", 600);

        setSkillRequirement("grand", 100);
        setSkillRequirement("double_creep", 300);
        setSkillRequirement("hammer_down", 500);
        setSkillRequirement("diastrophism", 600);
    }

    private static void setSkillRequirement(String skillName, int requiredXp) {
        for (SkillData skill : SwordSkillRegistry.SKILLS.values()) {
            if (skill.getName().equals(skillName)) {
                Map<String, Object> data = skill.getAdditionalData();

                Map<String, Integer> reqs;
                if (data.containsKey("proficiency_req")) {
                    reqs = (Map<String, Integer>) data.get("proficiency_req");
                } else {
                    reqs = new HashMap<>();
                    data.put("proficiency_req", reqs);
                }

                for (String type : ALL_WEAPON_TYPES) {
                    reqs.put(type, requiredXp);
                }
                break;
            }
        }
    }
}