import { CSSProperties } from 'vue';
import { createTypes, VueTypeValidableDef, VueTypesInterface } from 'vue-types';

type PropTypes = VueTypesInterface & {
  readonly style: VueTypeValidableDef<CSSProperties>;
};

const propTypes = createTypes({
  func: undefined,
  bool: undefined,
  string: undefined,
  number: undefined,
  object: undefined,
  integer: undefined
}) as PropTypes;

propTypes.extend([
  {
    name: 'style',
    getter: true,
    type: [String, Object],
    default: undefined
  }
]);
export { propTypes };
