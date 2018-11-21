package net.jqwik.properties.stateful;

import java.util.*;

import org.assertj.core.api.*;

import net.jqwik.api.*;
import net.jqwik.api.stateful.*;

class NSequentialActionSequenceTests {


	@Example
	void runSequence() {
		NSequentialActionSequence<Integer> sequence = createSequence( //
			plus1(), //
			ignore(), //
			plus10(), //
			ignore(), //
			plus100() //
		);

		int result = sequence.run(0);
		Assertions.assertThat(result).isEqualTo(111);
		Assertions.assertThat(result).isEqualTo(sequence.state());
	}

//	@Example
//	void runWithFailure() {
//		SequentialActionSequence<Integer> sequence = createSequence( //
//			plus1(), //
//			ignore(), //
//			plus10(), //
//			ignore(), //
//			check42(), plus100() //
//		);
//
//		Assertions.assertThatThrownBy(() -> //
//			sequence.run(0) //
//		).isInstanceOf(AssertionError.class);
//
//		Assertions.assertThat(sequence.state()).isEqualTo(11);
//	}
//
//	@Example
//	void failInInvariant() {
//		ActionSequence<Integer> sequence = createSequence( //
//			plus1(), //
//			plus10(), //
//			plus100() //
//		).withInvariant(anInt -> Assertions.assertThat(anInt).isLessThan(100));
//
//		Assertions.assertThatThrownBy(() -> //
//			sequence.run(0) //
//		).isInstanceOf(AssertionError.class);
//
//		Assertions.assertThat(sequence.state()).isEqualTo(111);
//
//	}

	private Action<Integer> check42() {
		return new Action<Integer>() {
			@Override
			public Integer run(Integer model) {
				Assertions.assertThat(model).isEqualTo(42);
				return model;
			}

			@Override
			public String toString() {
				return "check 42";
			}
		};
	}

	private Action<Integer> ignore() {
		return new Action<Integer>() {
			@Override
			public boolean precondition(Integer model) {
				return false;
			}

			@Override
			public Integer run(Integer model) {
				return null;
			}

			@Override
			public String toString() {
				return "ignore";
			}
		};
	}

	private Action<Integer> plus1() {
		return new Action<Integer>() {
			@Override
			public Integer run(Integer anInt) {
				return anInt + 1;
			}

			@Override
			public String toString() {
				return "+1";
			}
		};
	}

	private Action<Integer> plus10() {
		return new Action<Integer>() {
			@Override
			public Integer run(Integer anInt) {
				return anInt + 10;
			}

			@Override
			public String toString() {
				return "+10";
			}
		};
	}

	private Action<Integer> plus100() {
		return new Action<Integer>() {
			@Override
			public Integer run(Integer anInt) {
				return anInt + 100;
			}

			@Override
			public String toString() {
				return "+100";
			}
		};
	}

	@SuppressWarnings("unchecked")
	private NSequentialActionSequence<Integer> createSequence(Action<Integer>... actions) {
		List<Action<Integer>> list = Arrays.asList(actions);
		NActionGenerator<Integer> actionGenerator = model -> null;
		return new NSequentialActionSequence<>(actionGenerator, actions.length);
	}

}
